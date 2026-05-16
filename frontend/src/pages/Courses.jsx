import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import Modal from '../components/Modal';
import { fetchCourses, searchCourses, fetchCoursesBySemester, fetchCourseEnrollments, enrollInCourse, fetchCurrentStudent, fetchFaculty, createCourse, createStudent } from '../api';
import { Search, BookOpen, Clock, Users as UsersIcon, MapPin, Plus, Eye, UserPlus } from 'lucide-react';

export default function Courses() {
  const { user } = useAuth();
  const toast = useToast();
  const [courses, setCourses] = useState([]);
  const [filtered, setFiltered] = useState([]);
  const [keyword, setKeyword] = useState('');
  const [loading, setLoading] = useState(true);
  const [studentId, setStudentId] = useState(null);
  const [showAdd, setShowAdd] = useState(false);
  const [facultyList, setFacultyList] = useState([]);
  const [form, setForm] = useState({ courseCode: '', courseName: '', description: '', credits: 3, semester: 'Spring 2026', maxCapacity: 40, facultyId: '' });
  const [semesterFilter, setSemesterFilter] = useState('');
  // Enrollment viewer
  const [enrollModal, setEnrollModal] = useState(false);
  const [enrollCourse, setEnrollCourse] = useState(null);
  const [courseEnrollments, setCourseEnrollments] = useState([]);
  const [enrollLoading, setEnrollLoading] = useState(false);
  // Add Student modal (admin)
  const [showAddStudent, setShowAddStudent] = useState(false);
  const [studentForm, setStudentForm] = useState({
    firstName: '', lastName: '', dateOfBirth: '', address: '', department: 'Computer Science',
    enrollmentDate: new Date().toISOString().split('T')[0],
    username: '', email: '', phone: '', password: '',
  });

  useEffect(() => {
    loadCourses();
    if (user?.role === 'STUDENT') {
      fetchCurrentStudent().then(d => setStudentId(d.student?.studentId)).catch(() => {});
    }
    if (user?.role === 'ADMIN') {
      fetchFaculty().then(d => setFacultyList(d.faculty || [])).catch(() => {});
    }
  }, [user]);

  async function loadCourses() {
    setLoading(true);
    try {
      const data = await fetchCourses();
      setCourses(data.courses || []);
      setFiltered(data.courses || []);
    } catch (e) { toast.error('Failed to load courses'); }
    setLoading(false);
  }

  async function handleSearch() {
    if (!keyword.trim()) { setFiltered(courses); return; }
    try {
      const data = await searchCourses(keyword);
      setFiltered(data.courses || []);
    } catch (e) { toast.error('Search failed'); }
  }

  async function handleEnroll(courseId) {
    if (!studentId) { toast.error('Student profile not loaded'); return; }
    try {
      const data = await enrollInCourse(studentId, courseId);
      if (data.success) { toast.success(data.message); loadCourses(); }
      else toast.error(data.message);
    } catch (e) { toast.error('Enrollment failed'); }
  }

  async function handleAddCourse(e) {
    e.preventDefault();
    try {
      const data = await createCourse({ ...form, credits: +form.credits, maxCapacity: +form.maxCapacity, facultyId: +form.facultyId });
      if (data.success) { toast.success('Course created!'); setShowAdd(false); loadCourses(); }
      else toast.error(data.message);
    } catch (e) { toast.error('Failed to create course'); }
  }

  async function handleSemesterFilter(semester) {
    setSemesterFilter(semester);
    if (!semester) { setFiltered(courses); return; }
    try {
      const data = await fetchCoursesBySemester(semester);
      setFiltered(data.courses || []);
    } catch (e) { toast.error('Filter failed'); }
  }

  async function handleViewEnrollments(course) {
    setEnrollCourse(course);
    setEnrollModal(true);
    setEnrollLoading(true);
    try {
      const data = await fetchCourseEnrollments(course.courseId);
      setCourseEnrollments(data.success && data.enrollments ? data.enrollments : []);
    } catch (e) { setCourseEnrollments([]); }
    setEnrollLoading(false);
  }

  async function handleAddStudent(e) {
    e.preventDefault();
    try {
      const data = await createStudent({
        student: {
          firstName: studentForm.firstName, lastName: studentForm.lastName,
          dateOfBirth: studentForm.dateOfBirth, address: studentForm.address,
          department: studentForm.department, enrollmentDate: studentForm.enrollmentDate,
        },
        user: {
          username: studentForm.username, email: studentForm.email,
          phone: studentForm.phone, password: studentForm.password, role: 'ROLE_STUDENT',
        },
      });
      if (data.success) { toast.success(data.message || 'Student added!'); setShowAddStudent(false); }
      else toast.error(data.message);
    } catch (e) { toast.error('Failed to add student'); }
  }

  return (
    <>
      <div className="page-header animate-fade">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 12 }}>
          <div>
            <h1>{user?.role === 'STUDENT' ? 'Browse Courses' : 'Course Management'}</h1>
            <p>{filtered.length} courses available</p>
          </div>
          {user?.role === 'ADMIN' && (
            <div style={{ display: 'flex', gap: 10 }}>
              <button className="btn btn-primary" onClick={() => setShowAdd(true)}><Plus size={16} /> Add Course</button>
              <button className="btn btn-secondary" onClick={() => setShowAddStudent(true)}><UserPlus size={16} /> Add Student</button>
            </div>
          )}
        </div>
      </div>

      {/* Search + Semester Filter */}
      <div style={{ display: 'flex', gap: 10, marginBottom: 24, flexWrap: 'wrap' }}>
        <div style={{ flex: 1, position: 'relative', minWidth: 200 }}>
          <Search size={16} style={{ position: 'absolute', left: 14, top: '50%', transform: 'translateY(-50%)', color: 'var(--text-tertiary)' }} />
          <input className="form-input" placeholder="Search by code, name, or description..."
            value={keyword} onChange={e => setKeyword(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleSearch()}
            style={{ paddingLeft: 40 }} />
        </div>
        <select className="form-input" style={{ width: 'auto', minWidth: 160 }}
          value={semesterFilter} onChange={e => handleSemesterFilter(e.target.value)}>
          <option value="">All Semesters</option>
          <option>Spring 2024</option><option>Fall 2024</option>
          <option>Spring 2025</option><option>Fall 2025</option>
          <option>Spring 2026</option><option>Fall 2026</option>
        </select>
        <button className="btn btn-secondary" onClick={handleSearch}>Search</button>
      </div>

      {loading ? (
        <div className="loading-center"><div className="spinner" /></div>
      ) : filtered.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon"><BookOpen size={28} /></div>
          <div className="empty-state-title">No courses found</div>
          <p>Try a different search term</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(340px, 1fr))', gap: 16 }}>
          {filtered.map((c, i) => {
            const seats = c.maxCapacity - c.currentEnrollment;
            const fillPct = c.maxCapacity > 0 ? (c.currentEnrollment / c.maxCapacity) * 100 : 0;
            return (
              <div key={c.courseId} className="card" style={{ animation: `fadeIn 0.3s ease-out ${i * 0.05}s backwards` }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 12 }}>
                  <div>
                    <span className="badge badge-accent" style={{ marginBottom: 8, display: 'inline-block' }}>{c.courseCode}</span>
                    <h3 style={{ fontSize: 16, fontWeight: 600, color: 'var(--text-primary)', marginBottom: 4 }}>{c.courseName}</h3>
                  </div>
                  <span className="badge badge-info">{c.credits} cr</span>
                </div>
                {c.description && <p style={{ fontSize: 13, color: 'var(--text-tertiary)', marginBottom: 14, lineHeight: 1.5 }}>{c.description}</p>}
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 12, fontSize: 13, color: 'var(--text-secondary)', marginBottom: 14 }}>
                  {c.faculty && <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}><UsersIcon size={14} /> {c.faculty.firstName} {c.faculty.lastName}</span>}
                  <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}><Clock size={14} /> {c.semester}</span>
                  {c.roomNumber && <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}><MapPin size={14} /> {c.roomNumber}</span>}
                </div>
                {/* Capacity bar */}
                <div style={{ marginBottom: 14 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 12, color: 'var(--text-tertiary)', marginBottom: 4 }}>
                    <span>{c.currentEnrollment}/{c.maxCapacity} enrolled</span>
                    <span style={{ color: seats <= 5 ? 'var(--warning)' : 'var(--success)' }}>{seats} seats left</span>
                  </div>
                  <div style={{ height: 4, background: 'var(--bg-elevated)', borderRadius: 2 }}>
                    <div style={{
                      height: '100%', borderRadius: 2, width: `${Math.min(fillPct, 100)}%`,
                      background: fillPct > 90 ? 'var(--error)' : fillPct > 70 ? 'var(--warning)' : 'var(--success)',
                      transition: 'width 0.5s ease',
                    }} />
                  </div>
                </div>
                {user?.role === 'STUDENT' && (
                  <button className="btn btn-primary btn-sm" style={{ width: '100%', justifyContent: 'center' }}
                    onClick={() => handleEnroll(c.courseId)} disabled={seats <= 0}>
                    {seats <= 0 ? 'Full' : 'Enroll Now'}
                  </button>
                )}
                {(user?.role === 'ADMIN' || user?.role === 'FACULTY') && (
                  <button className="btn btn-sm" style={{ width: '100%', justifyContent: 'center', background: 'var(--bg-elevated)', color: 'var(--text-primary)', border: '1px solid var(--border)' }}
                    onClick={() => handleViewEnrollments(c)}>
                    <Eye size={14} /> View Enrollments
                  </button>
                )}
              </div>
            );
          })}
        </div>
      )}

      {/* Add Course Modal */}
      <Modal open={showAdd} onClose={() => setShowAdd(false)} title="Add New Course">
        <form onSubmit={handleAddCourse}>
          <div className="form-group">
            <label className="form-label">Course Code</label>
            <input className="form-input" required value={form.courseCode} onChange={e => setForm({ ...form, courseCode: e.target.value })} />
          </div>
          <div className="form-group">
            <label className="form-label">Course Name</label>
            <input className="form-input" required value={form.courseName} onChange={e => setForm({ ...form, courseName: e.target.value })} />
          </div>
          <div className="form-group">
            <label className="form-label">Description</label>
            <textarea className="form-input" rows={3} value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} />
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <div className="form-group">
              <label className="form-label">Credits</label>
              <input className="form-input" type="number" min={1} max={6} required value={form.credits} onChange={e => setForm({ ...form, credits: e.target.value })} />
            </div>
            <div className="form-group">
              <label className="form-label">Max Capacity</label>
              <input className="form-input" type="number" min={1} required value={form.maxCapacity} onChange={e => setForm({ ...form, maxCapacity: e.target.value })} />
            </div>
          </div>
          <div className="form-group">
            <label className="form-label">Semester</label>
            <select className="form-select" value={form.semester} onChange={e => setForm({ ...form, semester: e.target.value })}>
              <option>Spring 2025</option><option>Fall 2025</option><option>Spring 2026</option><option>Fall 2026</option>
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Faculty</label>
            <select className="form-select" required value={form.facultyId} onChange={e => setForm({ ...form, facultyId: e.target.value })}>
              <option value="">Select Faculty</option>
              {facultyList.map(f => <option key={f.facultyId} value={f.facultyId}>{f.firstName} {f.lastName} ({f.department})</option>)}
            </select>
          </div>
          <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setShowAdd(false)}>Cancel</button>
            <button type="submit" className="btn btn-primary">Create Course</button>
          </div>
        </form>
      </Modal>

      {/* View Enrollments Modal */}
      <Modal open={enrollModal} onClose={() => setEnrollModal(false)}
        title={enrollCourse ? `Enrollments: ${enrollCourse.courseCode} - ${enrollCourse.courseName}` : 'Enrollments'}>
        {enrollLoading ? (
          <div className="loading-center"><div className="spinner" /></div>
        ) : courseEnrollments.length > 0 ? (
          <div className="table-container">
            <table>
              <thead>
                <tr><th>Student</th><th>Status</th><th>Enrolled</th></tr>
              </thead>
              <tbody>
                {courseEnrollments.map((e, i) => (
                  <tr key={i}>
                    <td>{e.student?.firstName} {e.student?.lastName}</td>
                    <td><span className={`badge badge-${e.status?.toLowerCase() === 'active' ? 'success' : 'warning'}`}>{e.status}</span></td>
                    <td>{new Date(e.enrollmentDate).toLocaleDateString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : <div className="empty-state"><p>No enrollments found</p></div>}
      </Modal>

      {/* Add Student Modal (Admin) */}
      <Modal open={showAddStudent} onClose={() => setShowAddStudent(false)} title="Add New Student">
        <form onSubmit={handleAddStudent}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <div className="form-group">
              <label className="form-label">First Name</label>
              <input className="form-input" required value={studentForm.firstName} onChange={e => setStudentForm({ ...studentForm, firstName: e.target.value })} />
            </div>
            <div className="form-group">
              <label className="form-label">Last Name</label>
              <input className="form-input" required value={studentForm.lastName} onChange={e => setStudentForm({ ...studentForm, lastName: e.target.value })} />
            </div>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <div className="form-group">
              <label className="form-label">Date of Birth</label>
              <input className="form-input" type="date" required value={studentForm.dateOfBirth} onChange={e => setStudentForm({ ...studentForm, dateOfBirth: e.target.value })} />
            </div>
            <div className="form-group">
              <label className="form-label">Enrollment Date</label>
              <input className="form-input" type="date" required value={studentForm.enrollmentDate} onChange={e => setStudentForm({ ...studentForm, enrollmentDate: e.target.value })} />
            </div>
          </div>
          <div className="form-group">
            <label className="form-label">Address</label>
            <input className="form-input" required value={studentForm.address} onChange={e => setStudentForm({ ...studentForm, address: e.target.value })} />
          </div>
          <div className="form-group">
            <label className="form-label">Department</label>
            <select className="form-input" required value={studentForm.department} onChange={e => setStudentForm({ ...studentForm, department: e.target.value })}>
              <option>Computer Science</option><option>Electrical Engineering</option>
              <option>Mechanical Engineering</option><option>Civil Engineering</option>
              <option>Mathematics</option><option>Physics</option>
            </select>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <div className="form-group">
              <label className="form-label">Username</label>
              <input className="form-input" required value={studentForm.username} onChange={e => setStudentForm({ ...studentForm, username: e.target.value })} />
            </div>
            <div className="form-group">
              <label className="form-label">Email</label>
              <input className="form-input" type="email" required value={studentForm.email} onChange={e => setStudentForm({ ...studentForm, email: e.target.value })} />
            </div>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <div className="form-group">
              <label className="form-label">Phone</label>
              <input className="form-input" type="tel" required value={studentForm.phone} onChange={e => setStudentForm({ ...studentForm, phone: e.target.value })} />
            </div>
            <div className="form-group">
              <label className="form-label">Password</label>
              <input className="form-input" type="password" required value={studentForm.password} onChange={e => setStudentForm({ ...studentForm, password: e.target.value })} />
            </div>
          </div>
          <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end', marginTop: 8 }}>
            <button type="button" className="btn btn-secondary" onClick={() => setShowAddStudent(false)}>Cancel</button>
            <button type="submit" className="btn btn-primary">Add Student</button>
          </div>
        </form>
      </Modal>
    </>
  );
}
