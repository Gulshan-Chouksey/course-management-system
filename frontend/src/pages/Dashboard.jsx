import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import StatCard from '../components/StatCard';
import Modal from '../components/Modal';
import {
  BookOpen, Users, GraduationCap, ClipboardList, TrendingUp, Award, UserCheck, BarChart3,
  AlertCircle, Pencil, Plus
} from 'lucide-react';
import {
  PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, CartesianGrid,
  Tooltip, ResponsiveContainer, RadialBarChart, RadialBar, Legend
} from 'recharts';
import {
  fetchCourses, fetchStudents, fetchFaculty, fetchCurrentStudent,
  fetchStudentGrades, fetchStudentGPA, fetchStudentEnrollments, fetchMyCourses,
  fetchCourseEnrollments, fetchCourseGrades, fetchEnrollmentGrade, addGrade, updateGrade
} from '../api';

const CHART_COLORS = ['#6366f1', '#8b5cf6', '#06b6d4', '#10b981', '#f59e0b', '#ef4444', '#ec4899', '#3b82f6'];

const CustomTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  return (
    <div style={{
      background: 'var(--bg-card)', border: '1px solid var(--border)',
      borderRadius: 8, padding: '10px 14px', fontSize: 13,
    }}>
      <p style={{ color: 'var(--text-primary)', fontWeight: 600 }}>{label}</p>
      {payload.map((p, i) => (
        <p key={i} style={{ color: p.color }}>{p.name}: {p.value}</p>
      ))}
    </div>
  );
};

function AdminDashboard() {
  const [stats, setStats] = useState({ courses: 0, students: 0, faculty: 0, enrollments: 0 });
  const [courseData, setCourseData] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const [coursesRes, studentsRes, facultyRes] = await Promise.all([
          fetchCourses(), fetchStudents(), fetchFaculty(),
        ]);
        const courses = coursesRes.courses || [];
        const students = studentsRes.students || [];
        const faculty = facultyRes.faculty || [];
        const totalEnroll = courses.reduce((s, c) => s + (c.currentEnrollment || 0), 0);
        setStats({ courses: courses.length, students: students.length, faculty: faculty.length, enrollments: totalEnroll });

        // Department distribution
        const deptMap = {};
        students.forEach(s => { deptMap[s.department] = (deptMap[s.department] || 0) + 1; });
        setCourseData(Object.entries(deptMap).map(([name, value]) => ({ name, value })));
      } catch (e) { console.error(e); }
      setLoading(false);
    }
    load();
  }, []);

  if (loading) return <div className="loading-center"><div className="spinner" /></div>;

  return (
    <>
      <div className="page-header animate-fade">
        <h1>Admin Dashboard</h1>
        <p>Overview of your institution's performance</p>
      </div>

      <div className="stats-grid">
        <StatCard title="Total Courses" value={stats.courses} icon={BookOpen} gradient="var(--gradient-primary)" />
        <StatCard title="Total Students" value={stats.students} icon={Users} gradient="var(--gradient-cyan)" />
        <StatCard title="Total Faculty" value={stats.faculty} icon={UserCheck} gradient="var(--gradient-success)" />
        <StatCard title="Enrollments" value={stats.enrollments} icon={ClipboardList} gradient="var(--gradient-warm)" />
      </div>

      <div className="content-grid">
        <div className="chart-card animate-fade">
          <h3>Students by Department</h3>
          {courseData.length > 0 ? (
            <ResponsiveContainer width="100%" height={280}>
              <PieChart>
                <Pie data={courseData} cx="50%" cy="50%" innerRadius={60} outerRadius={100}
                  paddingAngle={4} dataKey="value" stroke="none">
                  {courseData.map((_, i) => <Cell key={i} fill={CHART_COLORS[i % CHART_COLORS.length]} />)}
                </Pie>
                <Tooltip content={<CustomTooltip />} />
                <Legend wrapperStyle={{ fontSize: 12, color: 'var(--text-secondary)' }} />
              </PieChart>
            </ResponsiveContainer>
          ) : <div className="empty-state"><p>No data available</p></div>}
        </div>

        <div className="chart-card animate-fade">
          <h3>Recent Activity</h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 12, padding: '8px 0' }}>
            {[
              { label: 'Active Courses', value: stats.courses, color: 'var(--accent)' },
              { label: 'Active Students', value: stats.students, color: 'var(--cyan)' },
              { label: 'Total Enrollments', value: stats.enrollments, color: 'var(--success)' },
            ].map((item, i) => (
              <div key={i} style={{
                display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                padding: '14px 16px', background: 'var(--bg-elevated)', borderRadius: 10,
                borderLeft: `3px solid ${item.color}`,
              }}>
                <span style={{ fontSize: 14, color: 'var(--text-secondary)' }}>{item.label}</span>
                <span style={{ fontSize: 20, fontWeight: 700, color: item.color }}>{item.value}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </>
  );
}

function StudentDashboard() {
  const [student, setStudent] = useState(null);
  const [grades, setGrades] = useState([]);
  const [gpa, setGpa] = useState(null);
  const [enrollments, setEnrollments] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const studentRes = await fetchCurrentStudent();
        const s = studentRes.student;
        setStudent(s);
        if (s?.studentId) {
          const [gradesRes, gpaRes, enrollRes] = await Promise.all([
            fetchStudentGrades(s.studentId),
            fetchStudentGPA(s.studentId),
            fetchStudentEnrollments(s.studentId),
          ]);
          setGrades(gradesRes.grades || []);
          setGpa(gpaRes.gpa);
          setEnrollments(enrollRes.enrollments || []);
        }
      } catch (e) { console.error(e); }
      setLoading(false);
    }
    load();
  }, []);

  if (loading) return <div className="loading-center"><div className="spinner" /></div>;

  // Grade distribution for pie chart
  const gradeMap = {};
  grades.forEach(g => { gradeMap[g.grade] = (gradeMap[g.grade] || 0) + 1; });
  const gradeDistData = Object.entries(gradeMap).map(([name, value]) => ({ name, value }));

  // Course marks for bar chart
  const marksData = grades.map(g => ({
    name: g.enrollment?.course?.courseCode || '?',
    Internal: g.internalMarks,
    External: g.externalMarks,
  }));

  // GPA data for radial chart
  const gpaData = gpa !== null ? [{ name: 'GPA', value: gpa, fill: gpa >= 3.5 ? '#10b981' : gpa >= 2.5 ? '#f59e0b' : '#ef4444' }] : [];

  const activeEnrollments = enrollments.filter(e => e.status === 'ACTIVE').length;

  return (
    <>
      <div className="page-header animate-fade">
        <h1>Welcome back, {student?.firstName || 'Student'} 👋</h1>
        <p>{student?.department || 'Department'} • {student?.enrollmentDate || 'Enrolled'}</p>
      </div>

      <div className="stats-grid">
        <StatCard title="Current GPA" value={gpa !== null ? gpa.toFixed(2) : '—'} icon={Award}
          gradient={gpa >= 3.5 ? 'var(--gradient-success)' : 'var(--gradient-warm)'} />
        <StatCard title="Enrolled Courses" value={activeEnrollments} icon={BookOpen} gradient="var(--gradient-primary)" />
        <StatCard title="Completed Grades" value={grades.length} icon={GraduationCap} gradient="var(--gradient-cyan)" />
        <StatCard title="Total Credits" value={grades.reduce((s, g) => s + (g.enrollment?.course?.credits || 0), 0)}
          icon={TrendingUp} gradient="var(--gradient-rose)" />
      </div>

      <div className="content-grid">
        {/* GPA Gauge */}
        <div className="chart-card animate-fade">
          <h3>GPA Overview</h3>
          {gpa !== null ? (
            <div style={{ textAlign: 'center' }}>
              <ResponsiveContainer width="100%" height={220}>
                <RadialBarChart cx="50%" cy="50%" innerRadius="60%" outerRadius="90%" startAngle={180} endAngle={0}
                  data={[{ value: 4, fill: 'var(--bg-elevated)' }, ...gpaData]} barSize={14}>
                  <RadialBar dataKey="value" cornerRadius={8} />
                </RadialBarChart>
              </ResponsiveContainer>
              <div style={{ marginTop: -40 }}>
                <div style={{ fontSize: 42, fontWeight: 800, color: gpaData[0]?.fill || 'var(--text-primary)' }}>
                  {gpa.toFixed(2)}
                </div>
                <div style={{ fontSize: 14, color: 'var(--text-tertiary)' }}>out of 4.00</div>
              </div>
            </div>
          ) : <div className="empty-state"><p>No GPA data yet</p></div>}
        </div>

        {/* Grade Distribution */}
        <div className="chart-card animate-fade">
          <h3>Grade Distribution</h3>
          {gradeDistData.length > 0 ? (
            <ResponsiveContainer width="100%" height={280}>
              <PieChart>
                <Pie data={gradeDistData} cx="50%" cy="50%" innerRadius={55} outerRadius={95}
                  paddingAngle={4} dataKey="value" stroke="none"
                  label={({ name, percent }) => `${name} (${(percent * 100).toFixed(0)}%)`}>
                  {gradeDistData.map((_, i) => <Cell key={i} fill={CHART_COLORS[i % CHART_COLORS.length]} />)}
                </Pie>
                <Tooltip content={<CustomTooltip />} />
              </PieChart>
            </ResponsiveContainer>
          ) : <div className="empty-state"><p>No grades yet</p></div>}
        </div>

        {/* Marks Breakdown */}
        <div className="chart-card animate-fade" style={{ gridColumn: '1 / -1' }}>
          <h3>Course-wise Marks Breakdown</h3>
          {marksData.length > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={marksData} barGap={4}>
                <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                <XAxis dataKey="name" tick={{ fill: 'var(--text-tertiary)', fontSize: 12 }} />
                <YAxis tick={{ fill: 'var(--text-tertiary)', fontSize: 12 }} />
                <Tooltip content={<CustomTooltip />} />
                <Legend wrapperStyle={{ fontSize: 12 }} />
                <Bar dataKey="Internal" fill="#6366f1" radius={[4, 4, 0, 0]} />
                <Bar dataKey="External" fill="#06b6d4" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          ) : <div className="empty-state"><p>No marks data yet</p></div>}
        </div>
      </div>
    </>
  );
}

function FacultyDashboard() {
  const toast = useToast();
  const [courses, setCourses] = useState([]);
  const [allEnrollments, setAllEnrollments] = useState([]);
  const [allGrades, setAllGrades] = useState([]);
  const [selectedCourse, setSelectedCourse] = useState('');
  const [courseEnrollments, setCourseEnrollments] = useState([]);
  const [loading, setLoading] = useState(true);

  // Grade modal state
  const [gradeModal, setGradeModal] = useState(false);
  const [gradeForm, setGradeForm] = useState({
    enrollmentId: '', internalMarks: '', externalMarks: '', remarks: '', gradeId: null,
  });

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    try {
      const data = await fetchMyCourses();
      const courseList = Array.isArray(data) ? data : [];
      setCourses(courseList);

      // Load all enrollments and grades for each course
      let enrolls = [];
      let grades = [];
      for (const course of courseList) {
        try {
          const enrollRes = await fetchCourseEnrollments(course.courseId);
          if (enrollRes.success && enrollRes.enrollments) {
            enrolls = enrolls.concat(enrollRes.enrollments);
          }
        } catch (e) { /* skip */ }
        try {
          const gradeRes = await fetchCourseGrades(course.courseId);
          if (gradeRes.success && gradeRes.grades) {
            grades = grades.concat(gradeRes.grades);
          }
        } catch (e) { /* skip */ }
      }
      setAllEnrollments(enrolls);
      setAllGrades(grades);
    } catch (e) { console.error(e); }
    setLoading(false);
  }

  async function handleCourseSelect(courseId) {
    setSelectedCourse(courseId);
    if (!courseId) { setCourseEnrollments([]); return; }
    try {
      const res = await fetchCourseEnrollments(courseId);
      setCourseEnrollments(res.success && res.enrollments ? res.enrollments : []);
    } catch (e) { setCourseEnrollments([]); }
  }

  function openGradeModal(enrollment, existingGrade) {
    setGradeForm({
      enrollmentId: enrollment?.enrollmentId || '',
      internalMarks: existingGrade?.internalMarks || '',
      externalMarks: existingGrade?.externalMarks || '',
      remarks: existingGrade?.remarks || '',
      gradeId: existingGrade?.gradeId || null,
    });
    setGradeModal(true);
  }

  async function handleGradeSubmit(e) {
    e.preventDefault();
    try {
      const payload = {
        enrollmentId: parseInt(gradeForm.enrollmentId),
        internalMarks: parseInt(gradeForm.internalMarks),
        externalMarks: parseInt(gradeForm.externalMarks),
        remarks: gradeForm.remarks,
      };
      let res;
      if (gradeForm.gradeId) {
        res = await updateGrade(gradeForm.gradeId, payload);
      } else {
        // Check if grade already exists
        try {
          const existing = await fetchEnrollmentGrade(gradeForm.enrollmentId);
          if (existing.success && existing.grade) {
            res = await updateGrade(existing.grade.gradeId, payload);
          } else {
            res = await addGrade(payload);
          }
        } catch {
          res = await addGrade(payload);
        }
      }
      if (res.success) {
        toast.success(res.message || 'Grade saved successfully');
        setGradeModal(false);
        loadData();
      } else {
        toast.error(res.message || 'Failed to save grade');
      }
    } catch (err) {
      toast.error('Error saving grade');
    }
  }

  if (loading) return <div className="loading-center"><div className="spinner" /></div>;

  const totalStudents = new Set(allEnrollments.map(e => e.student?.studentId)).size;
  const totalEnrollments = allEnrollments.length;
  const pendingGrades = allEnrollments.filter(e => !allGrades.some(g => g.enrollment?.enrollmentId === e.enrollmentId)).length;

  const enrollData = courses.map(c => ({
    name: c.courseCode,
    Students: c.currentEnrollment || 0,
    Capacity: c.maxCapacity || 0,
  }));

  // Recent enrollments (sorted by date)
  const recentEnrollments = [...allEnrollments]
    .sort((a, b) => new Date(b.enrollmentDate) - new Date(a.enrollmentDate))
    .slice(0, 5);

  return (
    <>
      <div className="page-header animate-fade">
        <h1>Faculty Dashboard</h1>
        <p>Manage your courses, students and grades</p>
      </div>

      <div className="stats-grid">
        <StatCard title="My Courses" value={courses.length} icon={BookOpen} gradient="var(--gradient-primary)" />
        <StatCard title="Total Students" value={totalStudents} icon={Users} gradient="var(--gradient-cyan)" />
        <StatCard title="Pending Grades" value={pendingGrades} icon={AlertCircle} gradient="var(--gradient-warm)" />
        <StatCard title="Total Enrollments" value={totalEnrollments} icon={ClipboardList} gradient="var(--gradient-success)" />
      </div>

      <div className="content-grid">
        {/* Enrollment vs Capacity chart */}
        <div className="chart-card animate-fade" style={{ gridColumn: '1 / -1' }}>
          <h3>Enrollment vs Capacity</h3>
          {enrollData.length > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={enrollData} barGap={4}>
                <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                <XAxis dataKey="name" tick={{ fill: 'var(--text-tertiary)', fontSize: 12 }} />
                <YAxis tick={{ fill: 'var(--text-tertiary)', fontSize: 12 }} />
                <Tooltip content={<CustomTooltip />} />
                <Legend wrapperStyle={{ fontSize: 12 }} />
                <Bar dataKey="Students" fill="#6366f1" radius={[4, 4, 0, 0]} />
                <Bar dataKey="Capacity" fill="#374151" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          ) : <div className="empty-state"><p>No courses assigned</p></div>}
        </div>

        {/* My Courses list */}
        <div className="chart-card animate-fade">
          <h3>My Courses</h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            {courses.map((c, i) => (
              <div key={i} style={{
                display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                padding: '14px 16px', background: 'var(--bg-elevated)', borderRadius: 10,
                borderLeft: '3px solid var(--accent)',
              }}>
                <div>
                  <div style={{ fontWeight: 600, fontSize: 14 }}>{c.courseName}</div>
                  <div style={{ fontSize: 12, color: 'var(--text-tertiary)' }}>{c.courseCode} • {c.semester}</div>
                </div>
                <span className="badge badge-info">{c.currentEnrollment} students</span>
              </div>
            ))}
            {courses.length === 0 && <div className="empty-state"><p>No courses</p></div>}
          </div>
        </div>

        {/* Recent Enrollments */}
        <div className="chart-card animate-fade">
          <h3>Recent Student Enrollments</h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            {recentEnrollments.map((e, i) => (
              <div key={i} style={{
                display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                padding: '12px 14px', background: 'var(--bg-elevated)', borderRadius: 8,
              }}>
                <div>
                  <div style={{ fontWeight: 600, fontSize: 13 }}>
                    {e.student?.firstName} {e.student?.lastName}
                  </div>
                  <div style={{ fontSize: 12, color: 'var(--text-tertiary)' }}>
                    {e.course?.courseCode} • Enrolled {new Date(e.enrollmentDate).toLocaleDateString()}
                  </div>
                </div>
                <span className="badge badge-success">{e.status}</span>
              </div>
            ))}
            {recentEnrollments.length === 0 && <div className="empty-state"><p>No recent enrollments</p></div>}
          </div>
        </div>

        {/* Assignments & Grading Table */}
        <div className="chart-card animate-fade" style={{ gridColumn: '1 / -1' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
            <h3 style={{ margin: 0 }}>Assignments & Grading</h3>
            <button className="btn btn-primary" style={{ padding: '8px 16px', fontSize: 13 }}
              onClick={() => { setGradeForm({ enrollmentId: '', internalMarks: '', externalMarks: '', remarks: '', gradeId: null }); setGradeModal(true); }}>
              <Plus size={15} /> Add Grade
            </button>
          </div>
          {allGrades.length > 0 ? (
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>Student</th>
                    <th>Course</th>
                    <th>Internal</th>
                    <th>External</th>
                    <th>Total</th>
                    <th>Grade</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {allGrades.map((g, i) => (
                    <tr key={i}>
                      <td>{g.enrollment?.student?.firstName} {g.enrollment?.student?.lastName}</td>
                      <td>{g.enrollment?.course?.courseCode}</td>
                      <td>{g.internalMarks}</td>
                      <td>{g.externalMarks}</td>
                      <td><strong>{g.totalMarks}</strong></td>
                      <td><span className="badge badge-info">{g.grade}</span></td>
                      <td>
                        <button className="btn" style={{ padding: '4px 10px', fontSize: 12, background: 'var(--bg-elevated)', color: 'var(--text-primary)', border: '1px solid var(--border)' }}
                          onClick={() => openGradeModal(g.enrollment, g)}>
                          <Pencil size={13} /> Edit
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : <div className="empty-state"><p>No grades found</p></div>}
        </div>

        {/* Course Enrollments Viewer */}
        <div className="chart-card animate-fade" style={{ gridColumn: '1 / -1' }}>
          <h3>Course Enrollments</h3>
          <div className="form-group" style={{ marginBottom: 16 }}>
            <label className="form-label">Select Course</label>
            <select className="form-input" value={selectedCourse} onChange={(e) => handleCourseSelect(e.target.value)}>
              <option value="">-- Select a course --</option>
              {courses.map(c => (
                <option key={c.courseId} value={c.courseId}>{c.courseCode} - {c.courseName}</option>
              ))}
            </select>
          </div>
          {selectedCourse && courseEnrollments.length > 0 ? (
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>Student Name</th>
                    <th>Email</th>
                    <th>Status</th>
                    <th>Enrollment Date</th>
                  </tr>
                </thead>
                <tbody>
                  {courseEnrollments.map((e, i) => (
                    <tr key={i}>
                      <td>{e.student?.firstName} {e.student?.lastName}</td>
                      <td>{e.student?.user?.email || 'N/A'}</td>
                      <td><span className={`badge badge-${e.status?.toLowerCase() === 'active' ? 'success' : 'warning'}`}>{e.status}</span></td>
                      <td>{new Date(e.enrollmentDate).toLocaleDateString()}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : selectedCourse ? <div className="empty-state"><p>No enrollments found for this course</p></div> : <div className="empty-state"><p>Select a course to view enrollments</p></div>}
        </div>
      </div>

      {/* Grade Modal */}
      <Modal open={gradeModal} onClose={() => setGradeModal(false)} title={gradeForm.gradeId ? 'Update Grade' : 'Add Grade'}>
        <form onSubmit={handleGradeSubmit}>
          <div className="form-group">
            <label className="form-label">Enrollment ID</label>
            <input type="number" className="form-input" value={gradeForm.enrollmentId}
              onChange={e => setGradeForm({ ...gradeForm, enrollmentId: e.target.value })} required
              disabled={!!gradeForm.gradeId} />
          </div>
          <div className="form-group">
            <label className="form-label">Internal Marks (0-100)</label>
            <input type="number" className="form-input" min="0" max="100"
              value={gradeForm.internalMarks}
              onChange={e => setGradeForm({ ...gradeForm, internalMarks: e.target.value })} required />
          </div>
          <div className="form-group">
            <label className="form-label">External Marks (0-100)</label>
            <input type="number" className="form-input" min="0" max="100"
              value={gradeForm.externalMarks}
              onChange={e => setGradeForm({ ...gradeForm, externalMarks: e.target.value })} required />
          </div>
          <div className="form-group">
            <label className="form-label">Remarks</label>
            <textarea className="form-input" rows={3} value={gradeForm.remarks}
              onChange={e => setGradeForm({ ...gradeForm, remarks: e.target.value })}
              placeholder="Optional remarks..." />
          </div>
          <div style={{ display: 'flex', gap: 10, marginTop: 16 }}>
            <button type="submit" className="btn btn-primary" style={{ flex: 1 }}>
              {gradeForm.gradeId ? 'Update Grade' : 'Submit Grade'}
            </button>
            <button type="button" className="btn" onClick={() => setGradeModal(false)}
              style={{ flex: 1, background: 'var(--bg-elevated)', color: 'var(--text-primary)', border: '1px solid var(--border)' }}>
              Cancel
            </button>
          </div>
        </form>
      </Modal>
    </>
  );
}

export default function Dashboard() {
  const { user } = useAuth();
  if (user?.role === 'ADMIN') return <AdminDashboard />;
  if (user?.role === 'FACULTY') return <FacultyDashboard />;
  return <StudentDashboard />;
}
