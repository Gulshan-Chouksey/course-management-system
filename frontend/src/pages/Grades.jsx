import { useState, useEffect } from 'react';
import { useToast } from '../context/ToastContext';
import { fetchCurrentStudent, fetchStudentGrades, fetchStudentGPA } from '../api';
import { GraduationCap, Award, TrendingUp, BookOpen } from 'lucide-react';
import { PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts';

const COLORS = ['#10b981', '#6366f1', '#3b82f6', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4', '#ec4899'];
const GRADE_COLORS = { 'A+': '#10b981', A: '#10b981', 'A-': '#22c55e', 'B+': '#3b82f6', B: '#3b82f6', 'B-': '#6366f1', C: '#f59e0b', D: '#ef4444', F: '#dc2626' };

const CustomTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  return (
    <div style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: 8, padding: '10px 14px', fontSize: 13 }}>
      <p style={{ color: 'var(--text-primary)', fontWeight: 600 }}>{label}</p>
      {payload.map((p, i) => <p key={i} style={{ color: p.color }}>{p.name}: {p.value}</p>)}
    </div>
  );
};

export default function Grades() {
  const toast = useToast();
  const [grades, setGrades] = useState([]);
  const [gpa, setGpa] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const sRes = await fetchCurrentStudent();
        const sid = sRes.student?.studentId;
        if (sid) {
          const [gRes, gpaRes] = await Promise.all([fetchStudentGrades(sid), fetchStudentGPA(sid)]);
          setGrades(gRes.grades || []);
          setGpa(gpaRes.gpa);
        }
      } catch (e) { toast.error('Failed to load grades'); }
      setLoading(false);
    }
    load();
  }, []);

  const gradeMap = {};
  grades.forEach(g => { gradeMap[g.grade] = (gradeMap[g.grade] || 0) + 1; });
  const pieData = Object.entries(gradeMap).map(([name, value]) => ({ name, value }));

  const barData = grades.map(g => ({
    name: g.enrollment?.course?.courseCode || '?',
    Total: g.totalMarks,
    Internal: g.internalMarks,
    External: g.externalMarks,
  }));

  const totalCredits = grades.reduce((s, g) => s + (g.enrollment?.course?.credits || 0), 0);
  const avgMarks = grades.length > 0 ? Math.round(grades.reduce((s, g) => s + g.totalMarks, 0) / grades.length) : 0;

  return (
    <>
      <div className="page-header animate-fade">
        <h1>My Grades</h1>
        <p>Academic performance overview</p>
      </div>

      {loading ? (
        <div className="loading-center"><div className="spinner" /></div>
      ) : grades.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon"><GraduationCap size={28} /></div>
          <div className="empty-state-title">No grades yet</div>
          <p>Grades will appear here once they are entered by faculty</p>
        </div>
      ) : (
        <>
          {/* Quick stats */}
          <div className="stats-grid">
            <div className="card" style={{ textAlign: 'center', padding: 20 }}>
              <Award size={24} color={gpa >= 3.5 ? 'var(--success)' : 'var(--warning)'} />
              <div style={{ fontSize: 32, fontWeight: 800, marginTop: 8, color: gpa >= 3.5 ? 'var(--success)' : 'var(--warning)' }}>
                {gpa !== null ? gpa.toFixed(2) : '—'}
              </div>
              <div style={{ fontSize: 13, color: 'var(--text-tertiary)' }}>Current GPA</div>
            </div>
            <div className="card" style={{ textAlign: 'center', padding: 20 }}>
              <BookOpen size={24} color="var(--accent)" />
              <div style={{ fontSize: 32, fontWeight: 800, marginTop: 8 }}>{grades.length}</div>
              <div style={{ fontSize: 13, color: 'var(--text-tertiary)' }}>Courses Graded</div>
            </div>
            <div className="card" style={{ textAlign: 'center', padding: 20 }}>
              <TrendingUp size={24} color="var(--cyan)" />
              <div style={{ fontSize: 32, fontWeight: 800, marginTop: 8 }}>{avgMarks}</div>
              <div style={{ fontSize: 13, color: 'var(--text-tertiary)' }}>Average Marks</div>
            </div>
            <div className="card" style={{ textAlign: 'center', padding: 20 }}>
              <GraduationCap size={24} color="var(--accent2)" />
              <div style={{ fontSize: 32, fontWeight: 800, marginTop: 8 }}>{totalCredits}</div>
              <div style={{ fontSize: 13, color: 'var(--text-tertiary)' }}>Total Credits</div>
            </div>
          </div>

          {/* Charts */}
          <div className="content-grid" style={{ marginBottom: 28 }}>
            <div className="chart-card animate-fade">
              <h3>Grade Distribution</h3>
              <ResponsiveContainer width="100%" height={260}>
                <PieChart>
                  <Pie data={pieData} cx="50%" cy="50%" innerRadius={50} outerRadius={90}
                    paddingAngle={4} dataKey="value" stroke="none"
                    label={({ name, percent }) => `${name} (${(percent * 100).toFixed(0)}%)`}>
                    {pieData.map((entry, i) => (
                      <Cell key={i} fill={GRADE_COLORS[entry.name] || COLORS[i % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip content={<CustomTooltip />} />
                </PieChart>
              </ResponsiveContainer>
            </div>
            <div className="chart-card animate-fade">
              <h3>Course Marks Comparison</h3>
              <ResponsiveContainer width="100%" height={260}>
                <BarChart data={barData} barGap={2}>
                  <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                  <XAxis dataKey="name" tick={{ fill: 'var(--text-tertiary)', fontSize: 11 }} />
                  <YAxis tick={{ fill: 'var(--text-tertiary)', fontSize: 11 }} />
                  <Tooltip content={<CustomTooltip />} />
                  <Legend wrapperStyle={{ fontSize: 12 }} />
                  <Bar dataKey="Internal" fill="#6366f1" radius={[3, 3, 0, 0]} />
                  <Bar dataKey="External" fill="#06b6d4" radius={[3, 3, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Grade Table */}
          <div className="chart-card animate-fade">
            <h3>Detailed Grades</h3>
            <div className="table-container" style={{ border: 'none' }}>
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Course</th><th>Internal</th><th>External</th><th>Total</th><th>Grade</th><th>Remarks</th>
                  </tr>
                </thead>
                <tbody>
                  {grades.map((g, i) => (
                    <tr key={i}>
                      <td>
                        <div style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{g.enrollment?.course?.courseName || '—'}</div>
                        <div style={{ fontSize: 12, color: 'var(--text-tertiary)' }}>{g.enrollment?.course?.courseCode}</div>
                      </td>
                      <td>{g.internalMarks}</td>
                      <td>{g.externalMarks}</td>
                      <td style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{g.totalMarks}</td>
                      <td>
                        <span style={{
                          display: 'inline-block', padding: '4px 12px', borderRadius: 6, fontWeight: 700, fontSize: 14,
                          background: (GRADE_COLORS[g.grade] || 'var(--accent)') + '22',
                          color: GRADE_COLORS[g.grade] || 'var(--accent)',
                        }}>{g.grade}</span>
                      </td>
                      <td style={{ fontSize: 13, color: 'var(--text-tertiary)' }}>{g.remarks || '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </>
      )}
    </>
  );
}
