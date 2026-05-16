import { useState, useEffect } from 'react';
import { useToast } from '../context/ToastContext';
import Modal from '../components/Modal';
import { fetchStudents, searchStudents, fetchStudentsByDept, fetchStudentEnrollments, fetchStudentGrades, fetchStudentGPA } from '../api';
import { Search, Users, GraduationCap, Mail, Phone, Building2, Eye, BarChart3 } from 'lucide-react';

const DEPARTMENTS = ['Computer Science', 'Electrical Engineering', 'Mechanical Engineering', 'Civil Engineering', 'Mathematics', 'Physics'];

export default function Students() {
  const toast = useToast();
  const [students, setStudents] = useState([]);
  const [filtered, setFiltered] = useState([]);
  const [keyword, setKeyword] = useState('');
  const [dept, setDept] = useState('');
  const [loading, setLoading] = useState(true);

  // Enrollment viewer modal
  const [enrollModal, setEnrollModal] = useState(false);
  const [enrollStudent, setEnrollStudent] = useState(null);
  const [enrollments, setEnrollments] = useState([]);
  const [enrollLoading, setEnrollLoading] = useState(false);

  // Grades viewer modal
  const [gradeModal, setGradeModal] = useState(false);
  const [gradeStudent, setGradeStudent] = useState(null);
  const [grades, setGrades] = useState([]);
  const [gpa, setGpa] = useState(null);
  const [gradeLoading, setGradeLoading] = useState(false);

  useEffect(() => { loadStudents(); }, []);

  async function loadStudents() {
    setLoading(true);
    try {
      const data = await fetchStudents();
      setStudents(data.students || []);
      setFiltered(data.students || []);
    } catch (e) { toast.error('Failed to load students'); }
    setLoading(false);
  }

  async function handleSearch() {
    if (!keyword.trim()) { setFiltered(students); return; }
    try {
      const data = await searchStudents(keyword);
      setFiltered(data.students || []);
    } catch (e) { toast.error('Search failed'); }
  }

  async function handleDeptFilter(d) {
    setDept(d);
    if (!d) { setFiltered(students); return; }
    try {
      const data = await fetchStudentsByDept(d);
      setFiltered(data.students || []);
    } catch (e) { toast.error('Filter failed'); }
  }

  async function handleViewEnrollments(student) {
    setEnrollStudent(student);
    setEnrollModal(true);
    setEnrollLoading(true);
    try {
      const data = await fetchStudentEnrollments(student.studentId);
      setEnrollments(data.success && data.enrollments ? data.enrollments : []);
    } catch (e) { setEnrollments([]); }
    setEnrollLoading(false);
  }

  async function handleViewGrades(student) {
    setGradeStudent(student);
    setGradeModal(true);
    setGradeLoading(true);
    try {
      const [gradesRes, gpaRes] = await Promise.all([
        fetchStudentGrades(student.studentId),
        fetchStudentGPA(student.studentId),
      ]);
      setGrades(gradesRes.success && gradesRes.grades ? gradesRes.grades : []);
      setGpa(gpaRes.success ? gpaRes.gpa : null);
    } catch (e) { setGrades([]); setGpa(null); }
    setGradeLoading(false);
  }

  return (
    <>
      <div className="page-header animate-fade">
        <h1>Student Management</h1>
        <p>{filtered.length} students found</p>
      </div>

      {/* Filters */}
      <div style={{ display: 'flex', gap: 10, marginBottom: 24, flexWrap: 'wrap' }}>
        <div style={{ flex: 1, minWidth: 200, position: 'relative' }}>
          <Search size={16} style={{ position: 'absolute', left: 14, top: '50%', transform: 'translateY(-50%)', color: 'var(--text-tertiary)' }} />
          <input className="form-input" placeholder="Search by name or email..."
            value={keyword} onChange={e => setKeyword(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleSearch()}
            style={{ paddingLeft: 40 }} />
        </div>
        <select className="form-select" style={{ width: 'auto', minWidth: 180 }}
          value={dept} onChange={e => handleDeptFilter(e.target.value)}>
          <option value="">All Departments</option>
          {DEPARTMENTS.map(d => <option key={d} value={d}>{d}</option>)}
        </select>
        <button className="btn btn-secondary" onClick={handleSearch}>Search</button>
      </div>

      {loading ? (
        <div className="loading-center"><div className="spinner" /></div>
      ) : filtered.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon"><Users size={28} /></div>
          <div className="empty-state-title">No students found</div>
          <p>Try adjusting your search filters</p>
        </div>
      ) : (
        <div className="table-container animate-fade">
          <table className="data-table">
            <thead>
              <tr>
                <th>Student</th>
                <th>Email</th>
                <th>Phone</th>
                <th>Department</th>
                <th>Enrolled</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((s, i) => (
                <tr key={s.studentId} style={{ animation: `fadeIn 0.3s ease-out ${i * 0.03}s backwards` }}>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                      <div style={{
                        width: 36, height: 36, borderRadius: 10,
                        background: 'var(--gradient-primary)',
                        display: 'flex', alignItems: 'center', justifyContent: 'center',
                        fontSize: 14, fontWeight: 600, color: 'white', flexShrink: 0,
                      }}>
                        {s.firstName?.[0]}{s.lastName?.[0]}
                      </div>
                      <div>
                        <div style={{ fontWeight: 600, color: 'var(--text-primary)', fontSize: 14 }}>{s.firstName} {s.lastName}</div>
                        <div style={{ fontSize: 12, color: 'var(--text-tertiary)' }}>ID: {s.studentId}</div>
                      </div>
                    </div>
                  </td>
                  <td>
                    <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                      <Mail size={14} /> {s.user?.email || '—'}
                    </span>
                  </td>
                  <td>
                    <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                      <Phone size={14} /> {s.user?.phone || '—'}
                    </span>
                  </td>
                  <td>
                    <span className="badge badge-accent" style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}>
                      <Building2 size={12} /> {s.department}
                    </span>
                  </td>
                  <td style={{ color: 'var(--text-tertiary)', fontSize: 13 }}>{s.enrollmentDate || '—'}</td>
                  <td>
                    <div style={{ display: 'flex', gap: 6 }}>
                      <button className="btn btn-sm" title="View Enrollments"
                        style={{ padding: '4px 10px', fontSize: 12, background: 'var(--bg-elevated)', color: 'var(--text-primary)', border: '1px solid var(--border)' }}
                        onClick={() => handleViewEnrollments(s)}>
                        <Eye size={13} /> Enrollments
                      </button>
                      <button className="btn btn-sm" title="View Grades"
                        style={{ padding: '4px 10px', fontSize: 12, background: 'var(--accent-muted)', color: 'var(--accent-hover)', border: '1px solid var(--accent-muted)' }}
                        onClick={() => handleViewGrades(s)}>
                        <BarChart3 size={13} /> Grades
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* View Enrollments Modal */}
      <Modal open={enrollModal} onClose={() => setEnrollModal(false)}
        title={enrollStudent ? `Enrollments: ${enrollStudent.firstName} ${enrollStudent.lastName}` : 'Enrollments'}>
        {enrollLoading ? (
          <div className="loading-center"><div className="spinner" /></div>
        ) : enrollments.length > 0 ? (
          <div className="table-container">
            <table>
              <thead>
                <tr><th>Course Code</th><th>Course Name</th><th>Status</th><th>Enrolled</th></tr>
              </thead>
              <tbody>
                {enrollments.map((e, i) => (
                  <tr key={i}>
                    <td>{e.course?.courseCode}</td>
                    <td>{e.course?.courseName}</td>
                    <td><span className={`badge badge-${e.status?.toLowerCase() === 'active' ? 'success' : e.status?.toLowerCase() === 'withdrawn' ? 'error' : 'info'}`}>{e.status}</span></td>
                    <td>{new Date(e.enrollmentDate).toLocaleDateString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : <div className="empty-state"><p>No enrollments found</p></div>}
      </Modal>

      {/* View Grades Modal */}
      <Modal open={gradeModal} onClose={() => setGradeModal(false)}
        title={gradeStudent ? `Grades: ${gradeStudent.firstName} ${gradeStudent.lastName}` : 'Grades'}>
        {gradeLoading ? (
          <div className="loading-center"><div className="spinner" /></div>
        ) : (
          <>
            {gpa !== null && (
              <div style={{
                padding: '14px 18px', background: 'var(--bg-elevated)', borderRadius: 10, marginBottom: 16,
                display: 'flex', justifyContent: 'space-between', alignItems: 'center',
              }}>
                <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>Overall GPA</span>
                <span style={{ fontSize: 22, fontWeight: 800, color: gpa >= 3.5 ? 'var(--success)' : gpa >= 2.5 ? 'var(--warning)' : 'var(--error)' }}>
                  {gpa.toFixed(2)} / 4.0
                </span>
              </div>
            )}
            {grades.length > 0 ? (
              <div className="table-container">
                <table>
                  <thead>
                    <tr><th>Course</th><th>Internal</th><th>External</th><th>Total</th><th>Grade</th><th>Remarks</th></tr>
                  </thead>
                  <tbody>
                    {grades.map((g, i) => (
                      <tr key={i}>
                        <td>{g.enrollment?.course?.courseCode}</td>
                        <td>{g.internalMarks}</td>
                        <td>{g.externalMarks}</td>
                        <td><strong>{g.totalMarks}</strong></td>
                        <td><span className="badge badge-info">{g.grade}</span></td>
                        <td style={{ color: 'var(--text-tertiary)' }}>{g.remarks || '—'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : <div className="empty-state"><p>No grades found</p></div>}
          </>
        )}
      </Modal>
    </>
  );
}
