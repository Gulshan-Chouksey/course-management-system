import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import {
  fetchCourses, fetchFaculty, fetchStudents, fetchEnrollmentReport,
  fetchGradeDistribution, fetchFacultyWorkload, fetchDepartmentReport, fetchStudentTranscript,
  fetchCurrentStudent
} from '../api';
import { exportCSV, exportPDF, buildTranscriptHTML } from '../utils/export';
import { BarChart3, FileText, Download } from 'lucide-react';
import {
  PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, CartesianGrid,
  Tooltip, ResponsiveContainer, Legend
} from 'recharts';

const COLORS = ['#6366f1', '#8b5cf6', '#06b6d4', '#10b981', '#f59e0b', '#ef4444', '#ec4899', '#3b82f6'];

const CustomTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  return (
    <div style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: 8, padding: '10px 14px', fontSize: 13 }}>
      <p style={{ color: 'var(--text-primary)', fontWeight: 600 }}>{label}</p>
      {payload.map((p, i) => <p key={i} style={{ color: p.color }}>{p.name || p.dataKey}: {p.value}</p>)}
    </div>
  );
};

export default function Reports() {
  const { user } = useAuth();
  const toast = useToast();
  const [tab, setTab] = useState(user?.role === 'STUDENT' ? 'transcript' : 'enrollment');
  const [courses, setCourses] = useState([]);
  const [faculty, setFaculty] = useState([]);
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(false);
  const [reportData, setReportData] = useState(null);
  const [selectedId, setSelectedId] = useState('');
  const [deptInput, setDeptInput] = useState('');
  const [studentId, setStudentId] = useState(null);

  useEffect(() => {
    fetchCourses().then(d => setCourses(d.courses || [])).catch(() => {});
    if (user?.role !== 'STUDENT') {
      fetchFaculty().then(d => setFaculty(d.faculty || [])).catch(() => {});
      fetchStudents().then(d => setStudents(d.students || [])).catch(() => {});
    }
    if (user?.role === 'STUDENT') {
      fetchCurrentStudent().then(d => {
        setStudentId(d.student?.studentId);
        if (d.student?.studentId) loadTranscript(d.student.studentId);
      }).catch(() => {});
    }
  }, [user]);

  const tabs = user?.role === 'STUDENT'
    ? [{ id: 'transcript', label: 'My Transcript' }]
    : [
        { id: 'enrollment', label: 'Enrollment' },
        { id: 'grades', label: 'Grade Distribution' },
        { id: 'faculty', label: 'Faculty Workload' },
        { id: 'department', label: 'Department' },
        { id: 'transcript', label: 'Transcript' },
      ];

  function switchTab(t) { setTab(t); setReportData(null); setSelectedId(''); }

  async function loadEnrollment(courseId) {
    setSelectedId(courseId); setLoading(true); setReportData(null);
    try {
      const data = await fetchEnrollmentReport(courseId);
      setReportData(data);
    } catch (e) { toast.error('Failed to load report'); }
    setLoading(false);
  }

  async function loadGradeDist(courseId) {
    setSelectedId(courseId); setLoading(true); setReportData(null);
    try {
      const data = await fetchGradeDistribution(courseId);
      setReportData(data);
    } catch (e) { toast.error('Failed to load report'); }
    setLoading(false);
  }

  async function loadFacultyWork(fId) {
    setSelectedId(fId); setLoading(true); setReportData(null);
    try {
      const data = await fetchFacultyWorkload(fId);
      setReportData(data);
    } catch (e) { toast.error('Failed to load report'); }
    setLoading(false);
  }

  async function loadDeptReport() {
    if (!deptInput.trim()) { toast.warning('Enter department name'); return; }
    setLoading(true); setReportData(null);
    try {
      const data = await fetchDepartmentReport(deptInput);
      setReportData(data);
    } catch (e) { toast.error('Failed to load report'); }
    setLoading(false);
  }

  async function loadTranscript(sid) {
    setLoading(true); setReportData(null);
    try {
      const data = await fetchStudentTranscript(sid || selectedId);
      setReportData(data);
    } catch (e) { toast.error('Failed to load transcript'); }
    setLoading(false);
  }

  return (
    <>
      <div className="page-header animate-fade">
        <h1>{user?.role === 'STUDENT' ? 'My Transcript' : 'Reports & Analytics'}</h1>
        <p>Detailed academic insights with visual analytics</p>
      </div>

      {/* Tabs */}
      {tabs.length > 1 && (
        <div className="tabs">
          {tabs.map(t => (
            <button key={t.id} className={`tab-btn ${tab === t.id ? 'active' : ''}`} onClick={() => switchTab(t.id)}>
              {t.label}
            </button>
          ))}
        </div>
      )}

      {/* Enrollment Tab */}
      {tab === 'enrollment' && (
        <div className="animate-fade">
          <div className="form-group" style={{ maxWidth: 400 }}>
            <label className="form-label">Select Course</label>
            <select className="form-select" value={selectedId} onChange={e => loadEnrollment(e.target.value)}>
              <option value="">-- Select a course --</option>
              {courses.map(c => <option key={c.courseId} value={c.courseId}>{c.courseCode} - {c.courseName}</option>)}
            </select>
          </div>
          {loading && <div className="loading-center"><div className="spinner" /></div>}
          {reportData?.success && reportData.students && (
            <div className="chart-card" style={{ marginTop: 16 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h3>Enrolled Students ({reportData.totalStudents})</h3>
                <button className="btn btn-sm" style={{ background: 'var(--bg-elevated)', color: 'var(--text-primary)', border: '1px solid var(--border)' }}
                  onClick={() => exportCSV(
                    [{key:'name',label:'Name'},{key:'email',label:'Email'},{key:'dept',label:'Department'}],
                    reportData.students.map(s => ({ name: `${s.firstName} ${s.lastName}`, email: s.user?.email||'', dept: s.department })),
                    'enrollment_report'
                  )}><Download size={14} /> Export CSV</button>
              </div>
              <div className="table-container" style={{ border: 'none', marginTop: 12 }}>
                <table className="data-table">
                  <thead><tr><th>Name</th><th>Email</th><th>Department</th></tr></thead>
                  <tbody>
                    {reportData.students.map(s => (
                      <tr key={s.studentId}>
                        <td style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{s.firstName} {s.lastName}</td>
                        <td>{s.user?.email || '—'}</td>
                        <td><span className="badge badge-accent">{s.department}</span></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Grade Distribution Tab */}
      {tab === 'grades' && (
        <div className="animate-fade">
          <div className="form-group" style={{ maxWidth: 400 }}>
            <label className="form-label">Select Course</label>
            <select className="form-select" value={selectedId} onChange={e => loadGradeDist(e.target.value)}>
              <option value="">-- Select a course --</option>
              {courses.map(c => <option key={c.courseId} value={c.courseId}>{c.courseCode} - {c.courseName}</option>)}
            </select>
          </div>
          {loading && <div className="loading-center"><div className="spinner" /></div>}
          {reportData?.success && reportData.distribution && (
            <div className="content-grid" style={{ marginTop: 16 }}>
              <div className="chart-card">
                <h3>Grade Distribution (Total: {reportData.totalGraded})</h3>
                <ResponsiveContainer width="100%" height={300}>
                  <PieChart>
                    <Pie
                      data={Object.entries(reportData.distribution).map(([name, value]) => ({ name, value }))}
                      cx="50%" cy="50%" innerRadius={55} outerRadius={100}
                      paddingAngle={4} dataKey="value" stroke="none"
                      label={({ name, percent }) => `${name} (${(percent * 100).toFixed(0)}%)`}
                    >
                      {Object.entries(reportData.distribution).map((_, i) => (
                        <Cell key={i} fill={COLORS[i % COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip content={<CustomTooltip />} />
                    <Legend wrapperStyle={{ fontSize: 12 }} />
                  </PieChart>
                </ResponsiveContainer>
              </div>
              <div className="chart-card">
                <h3>Grade Counts</h3>
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={Object.entries(reportData.distribution).map(([name, count]) => ({ name, count }))}>
                    <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                    <XAxis dataKey="name" tick={{ fill: 'var(--text-tertiary)', fontSize: 12 }} />
                    <YAxis tick={{ fill: 'var(--text-tertiary)', fontSize: 12 }} allowDecimals={false} />
                    <Tooltip content={<CustomTooltip />} />
                    <Bar dataKey="count" fill="#6366f1" radius={[6, 6, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Faculty Workload Tab */}
      {tab === 'faculty' && (
        <div className="animate-fade">
          <div className="form-group" style={{ maxWidth: 400 }}>
            <label className="form-label">Select Faculty</label>
            <select className="form-select" value={selectedId} onChange={e => loadFacultyWork(e.target.value)}>
              <option value="">-- Select faculty --</option>
              {faculty.map(f => <option key={f.facultyId} value={f.facultyId}>{f.firstName} {f.lastName} ({f.department})</option>)}
            </select>
          </div>
          {loading && <div className="loading-center"><div className="spinner" /></div>}
          {reportData?.success && reportData.courses && (
            <div className="content-grid" style={{ marginTop: 16 }}>
              <div className="chart-card" style={{ gridColumn: '1 / -1' }}>
                <h3>Courses & Enrollment ({reportData.totalCourses} courses)</h3>
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={reportData.courses.map(c => ({ name: c.courseCode, Students: c.currentEnrollment, Credits: c.credits }))}>
                    <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                    <XAxis dataKey="name" tick={{ fill: 'var(--text-tertiary)', fontSize: 12 }} />
                    <YAxis tick={{ fill: 'var(--text-tertiary)', fontSize: 12 }} />
                    <Tooltip content={<CustomTooltip />} />
                    <Legend wrapperStyle={{ fontSize: 12 }} />
                    <Bar dataKey="Students" fill="#6366f1" radius={[4, 4, 0, 0]} />
                    <Bar dataKey="Credits" fill="#06b6d4" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Department Tab */}
      {tab === 'department' && (
        <div className="animate-fade">
          <div style={{ display: 'flex', gap: 10, maxWidth: 500, marginBottom: 16 }}>
            <input className="form-input" placeholder="e.g., Computer Science"
              value={deptInput} onChange={e => setDeptInput(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && loadDeptReport()} />
            <button className="btn btn-primary" onClick={loadDeptReport}>Generate</button>
          </div>
          {loading && <div className="loading-center"><div className="spinner" /></div>}
          {reportData?.success && reportData.report && (
            <div className="content-grid" style={{ marginTop: 8 }}>
              <div className="chart-card">
                <h3>{reportData.report.departmentName}</h3>
                <div style={{ display: 'flex', flexDirection: 'column', gap: 10, marginTop: 8 }}>
                  {[
                    { label: 'Total Students', value: reportData.report.totalStudents, color: 'var(--accent)' },
                    { label: 'Total Faculty', value: reportData.report.totalFaculty, color: 'var(--cyan)' },
                    { label: 'Average GPA', value: reportData.report.averageGpa?.toFixed(2) || 'N/A', color: 'var(--success)' },
                    { label: 'Pass Rate', value: reportData.report.passRate ? reportData.report.passRate.toFixed(1) + '%' : 'N/A', color: 'var(--warning)' },
                  ].map((item, i) => (
                    <div key={i} style={{
                      display: 'flex', justifyContent: 'space-between', padding: '14px 16px',
                      background: 'var(--bg-elevated)', borderRadius: 10, borderLeft: `3px solid ${item.color}`,
                    }}>
                      <span style={{ color: 'var(--text-secondary)' }}>{item.label}</span>
                      <span style={{ fontWeight: 700, color: item.color }}>{item.value}</span>
                    </div>
                  ))}
                </div>
              </div>
              {reportData.report.gradeDistribution && (
                <div className="chart-card">
                  <h3>Grade Distribution</h3>
                  <ResponsiveContainer width="100%" height={280}>
                    <PieChart>
                      <Pie
                        data={Object.entries(reportData.report.gradeDistribution).map(([name, value]) => ({ name, value }))}
                        cx="50%" cy="50%" innerRadius={50} outerRadius={90}
                        paddingAngle={4} dataKey="value" stroke="none"
                        label={({ name, percent }) => `${name} (${(percent * 100).toFixed(0)}%)`}
                      >
                        {Object.entries(reportData.report.gradeDistribution).map((_, i) => (
                          <Cell key={i} fill={COLORS[i % COLORS.length]} />
                        ))}
                      </Pie>
                      <Tooltip content={<CustomTooltip />} />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
              )}
            </div>
          )}
        </div>
      )}

      {/* Transcript Tab */}
      {tab === 'transcript' && (
        <div className="animate-fade">
          {user?.role !== 'STUDENT' && (
            <div className="form-group" style={{ maxWidth: 400 }}>
              <label className="form-label">Select Student</label>
              <select className="form-select" value={selectedId} onChange={e => { setSelectedId(e.target.value); loadTranscript(e.target.value); }}>
                <option value="">-- Select a student --</option>
                {students.map(s => <option key={s.studentId} value={s.studentId}>{s.firstName} {s.lastName} ({s.department})</option>)}
              </select>
            </div>
          )}
          {loading && <div className="loading-center"><div className="spinner" /></div>}
          {reportData?.success && reportData.transcript && (
            <div className="chart-card" style={{ marginTop: 8 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 16, marginBottom: 24 }}>
                <div>
                  <h2 style={{ fontSize: 22, fontWeight: 700, marginBottom: 4 }}>{reportData.transcript.studentName}</h2>
                  <p style={{ color: 'var(--text-secondary)', fontSize: 14 }}>
                    {reportData.transcript.department} • ID: {reportData.transcript.studentId}
                  </p>
                </div>
                <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', alignItems: 'flex-start' }}>
                  <div style={{ display: 'flex', gap: 8 }}>
                    <button className="btn btn-sm" style={{ background: 'var(--bg-elevated)', color: 'var(--text-primary)', border: '1px solid var(--border)' }}
                      onClick={() => exportCSV(
                        [{key:'code',label:'Code'},{key:'name',label:'Course'},{key:'semester',label:'Semester'},{key:'credits',label:'Credits'},{key:'internal',label:'Internal'},{key:'external',label:'External'},{key:'total',label:'Total'},{key:'grade',label:'Grade'}],
                        (reportData.transcript.courses||[]).map(c => ({ code:c.courseCode, name:c.courseName, semester:c.semester||'', credits:c.credits, internal:c.internalMarks, external:c.externalMarks, total:c.totalMarks, grade:c.grade })),
                        `transcript_${reportData.transcript.studentName.replace(/\s/g,'_')}`
                      )}><Download size={14} /> CSV</button>
                    <button className="btn btn-sm" style={{ background: 'var(--accent-muted)', color: 'var(--accent)', border: '1px solid var(--accent-muted)' }}
                      onClick={() => exportPDF(`Transcript - ${reportData.transcript.studentName}`, buildTranscriptHTML(reportData.transcript))}>
                      <FileText size={14} /> PDF
                    </button>
                  </div>
                  <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap' }}>
                    {[
                      { label: 'GPA', value: reportData.transcript.gpa?.toFixed(2) || '—', color: 'var(--success)' },
                      { label: 'Courses', value: reportData.transcript.totalCourses || 0, color: 'var(--accent)' },
                      { label: 'Credits', value: reportData.transcript.totalCredits || 0, color: 'var(--cyan)' },
                    ].map((s, i) => (
                      <div key={i} style={{
                        textAlign: 'center', padding: '10px 20px',
                        background: 'var(--bg-elevated)', borderRadius: 10,
                      }}>
                        <div style={{ fontSize: 24, fontWeight: 800, color: s.color }}>{s.value}</div>
                        <div style={{ fontSize: 11, color: 'var(--text-tertiary)', marginTop: 2 }}>{s.label}</div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>

              {reportData.transcript.courses?.length > 0 ? (
                <div className="table-container" style={{ border: 'none' }}>
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>Course</th><th>Semester</th><th>Credits</th>
                        <th>Internal</th><th>External</th><th>Total</th><th>Grade</th>
                      </tr>
                    </thead>
                    <tbody>
                      {reportData.transcript.courses.map((c, i) => (
                        <tr key={i}>
                          <td>
                            <div style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{c.courseName}</div>
                            <div style={{ fontSize: 12, color: 'var(--text-tertiary)' }}>{c.courseCode}</div>
                          </td>
                          <td>{c.semester || '—'}</td>
                          <td>{c.credits}</td>
                          <td>{c.internalMarks}</td>
                          <td>{c.externalMarks}</td>
                          <td style={{ fontWeight: 600 }}>{c.totalMarks}</td>
                          <td>
                            <span style={{
                              padding: '4px 12px', borderRadius: 6, fontWeight: 700,
                              background: 'var(--accent-muted)', color: 'var(--accent)',
                            }}>{c.grade}</span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                <div className="empty-state"><p>No course grades found</p></div>
              )}
            </div>
          )}
        </div>
      )}

      {/* Empty state when no report loaded */}
      {!loading && !reportData && tab !== 'transcript' && (
        <div className="empty-state" style={{ marginTop: 40 }}>
          <div className="empty-state-icon"><BarChart3 size={28} /></div>
          <div className="empty-state-title">Select an option above</div>
          <p>Choose a filter to generate the report</p>
        </div>
      )}
      {!loading && !reportData && tab === 'transcript' && user?.role === 'STUDENT' && (
        <div className="empty-state" style={{ marginTop: 40 }}>
          <div className="empty-state-icon"><FileText size={28} /></div>
          <div className="empty-state-title">Loading transcript...</div>
        </div>
      )}
    </>
  );
}
