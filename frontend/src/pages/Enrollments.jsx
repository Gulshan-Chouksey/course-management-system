import { useState, useEffect } from 'react';
import { useToast } from '../context/ToastContext';
import { fetchCurrentStudent, fetchStudentEnrollments, withdrawEnrollment } from '../api';
import { ClipboardList, BookOpen, Calendar, AlertTriangle } from 'lucide-react';

const STATUS_BADGE = {
  ACTIVE: 'badge-success',
  WITHDRAWN: 'badge-error',
  COMPLETED: 'badge-info',
};

export default function Enrollments() {
  const toast = useToast();
  const [enrollments, setEnrollments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [studentId, setStudentId] = useState(null);

  useEffect(() => {
    async function load() {
      try {
        const res = await fetchCurrentStudent();
        const sid = res.student?.studentId;
        setStudentId(sid);
        if (sid) {
          const data = await fetchStudentEnrollments(sid);
          setEnrollments(data.enrollments || []);
        }
      } catch (e) { toast.error('Failed to load enrollments'); }
      setLoading(false);
    }
    load();
  }, []);

  async function handleWithdraw(enrollmentId) {
    if (!window.confirm('Are you sure you want to withdraw from this course?')) return;
    try {
      const data = await withdrawEnrollment(enrollmentId);
      if (data.success) {
        toast.success(data.message);
        const res = await fetchStudentEnrollments(studentId);
        setEnrollments(res.enrollments || []);
      } else toast.error(data.message);
    } catch (e) { toast.error('Withdrawal failed'); }
  }

  const active = enrollments.filter(e => e.status === 'ACTIVE');
  const others = enrollments.filter(e => e.status !== 'ACTIVE');

  return (
    <>
      <div className="page-header animate-fade">
        <h1>My Enrollments</h1>
        <p>{active.length} active courses • {enrollments.length} total</p>
      </div>

      {loading ? (
        <div className="loading-center"><div className="spinner" /></div>
      ) : enrollments.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon"><ClipboardList size={28} /></div>
          <div className="empty-state-title">No enrollments yet</div>
          <p>Browse courses to start enrolling</p>
        </div>
      ) : (
        <>
          {/* Active enrollments */}
          {active.length > 0 && (
            <div style={{ marginBottom: 28 }}>
              <h2 style={{ fontSize: 16, fontWeight: 600, color: 'var(--text-primary)', marginBottom: 14 }}>
                Active Courses
              </h2>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(340px, 1fr))', gap: 14 }}>
                {active.map((e, i) => {
                  const c = e.course;
                  const faculty = c.faculty ? `${c.faculty.firstName} ${c.faculty.lastName}` : 'TBA';
                  return (
                    <div key={e.enrollmentId} className="card" style={{ animation: `fadeIn 0.3s ease-out ${i * 0.05}s backwards` }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 12 }}>
                        <span className="badge badge-accent">{c.courseCode}</span>
                        <span className={`badge ${STATUS_BADGE[e.status] || 'badge-info'}`}>{e.status}</span>
                      </div>
                      <h3 style={{ fontSize: 16, fontWeight: 600, marginBottom: 8 }}>{c.courseName}</h3>
                      <div style={{ display: 'flex', flexDirection: 'column', gap: 6, fontSize: 13, color: 'var(--text-secondary)', marginBottom: 14 }}>
                        <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}><BookOpen size={14} /> {faculty}</span>
                        <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}><Calendar size={14} /> {new Date(e.enrollmentDate).toLocaleDateString()}</span>
                      </div>
                      <div style={{ display: 'flex', gap: 8 }}>
                        <span className="badge badge-info">{c.credits} Credits</span>
                        <span className="badge badge-warning">{c.semester}</span>
                      </div>
                      <button className="btn btn-danger btn-sm" style={{ width: '100%', justifyContent: 'center', marginTop: 14 }}
                        onClick={() => handleWithdraw(e.enrollmentId)}>
                        <AlertTriangle size={14} /> Withdraw
                      </button>
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {/* Other enrollments */}
          {others.length > 0 && (
            <div>
              <h2 style={{ fontSize: 16, fontWeight: 600, color: 'var(--text-primary)', marginBottom: 14 }}>Past Enrollments</h2>
              <div className="table-container animate-fade">
                <table className="data-table">
                  <thead>
                    <tr><th>Course</th><th>Faculty</th><th>Status</th><th>Date</th></tr>
                  </thead>
                  <tbody>
                    {others.map(e => (
                      <tr key={e.enrollmentId}>
                        <td>
                          <div style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{e.course.courseName}</div>
                          <div style={{ fontSize: 12, color: 'var(--text-tertiary)' }}>{e.course.courseCode}</div>
                        </td>
                        <td>{e.course.faculty ? `${e.course.faculty.firstName} ${e.course.faculty.lastName}` : '—'}</td>
                        <td><span className={`badge ${STATUS_BADGE[e.status] || 'badge-info'}`}>{e.status}</span></td>
                        <td style={{ fontSize: 13 }}>{new Date(e.enrollmentDate).toLocaleDateString()}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </>
      )}
    </>
  );
}
