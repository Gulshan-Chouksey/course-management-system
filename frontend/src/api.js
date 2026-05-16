const API_BASE = '/api';

async function handleResponse(response) {
  if (response.status === 401 || response.status === 403) {
    window.location.href = '/';
    throw new Error('Unauthorized');
  }
  const data = await response.json();
  return data;
}

function getHeaders() {
  return { 'Content-Type': 'application/json' };
}

// ===== AUTH =====
export async function loginUser(username, password) {
  const formData = new URLSearchParams();
  formData.append('username', username);
  formData.append('password', password);

  const res = await fetch('/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: formData.toString(),
    credentials: 'include',
    redirect: 'manual',
  });

  // Status 0 = opaque redirect (login succeeded, Spring is redirecting)
  // Status 302 = redirect (login succeeded)
  // Status 200 = stayed on login page (could be error page)
  if (res.type === 'opaqueredirect' || res.status === 302 || res.status === 0) {
    // Login succeeded! Now determine role by trying API endpoints
    try {
      const studentRes = await fetch('/api/students/me', { credentials: 'include' });
      if (studentRes.ok) {
        const data = await studentRes.json();
        if (data.success) return { role: 'STUDENT' };
      }
    } catch (e) { /* not a student */ }

    try {
      const facultyRes = await fetch('/api/courses/my-courses', { credentials: 'include' });
      if (facultyRes.ok) {
        const data = await facultyRes.json();
        if (Array.isArray(data)) return { role: 'FACULTY' };
      }
    } catch (e) { /* not faculty */ }

    // If neither student nor faculty, must be admin
    return { role: 'ADMIN' };
  }

  // If we get a 200 with the login page content, login failed
  if (res.status === 200) {
    const text = await res.text();
    if (text.includes('error') || text.includes('login')) {
      throw new Error('Invalid username or password');
    }
  }

  throw new Error('Login failed');
}

export async function logoutUser() {
  const formData = new URLSearchParams();
  await fetch('/logout', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: formData.toString(),
    credentials: 'include',
    redirect: 'follow',
  });
}

// ===== COURSES =====
export async function fetchCourses() {
  const res = await fetch(`${API_BASE}/courses`, { credentials: 'include' });
  return handleResponse(res);
}

export async function searchCourses(keyword) {
  const res = await fetch(`${API_BASE}/courses/search?keyword=${encodeURIComponent(keyword)}`, { credentials: 'include' });
  return handleResponse(res);
}

export async function fetchCoursesBySemester(semester) {
  const res = await fetch(`${API_BASE}/courses/semester/${encodeURIComponent(semester)}`, { credentials: 'include' });
  return handleResponse(res);
}

export async function fetchMyCourses() {
  const res = await fetch(`${API_BASE}/courses/my-courses`, { credentials: 'include' });
  return res.json();
}

export async function fetchCourseEnrollments(courseId) {
  const res = await fetch(`${API_BASE}/courses/${courseId}/enrollments`, { credentials: 'include' });
  return handleResponse(res);
}

export async function createCourse(courseData) {
  const res = await fetch(`${API_BASE}/courses`, {
    method: 'POST', headers: getHeaders(), credentials: 'include',
    body: JSON.stringify(courseData),
  });
  return handleResponse(res);
}

// ===== STUDENTS =====
export async function fetchStudents() {
  const res = await fetch(`${API_BASE}/students`, { credentials: 'include' });
  return handleResponse(res);
}

export async function fetchCurrentStudent() {
  const res = await fetch(`${API_BASE}/students/me`, { credentials: 'include' });
  return handleResponse(res);
}

export async function searchStudents(keyword) {
  const res = await fetch(`${API_BASE}/students/search?keyword=${encodeURIComponent(keyword)}`, { credentials: 'include' });
  return handleResponse(res);
}

export async function fetchStudentsByDept(dept) {
  const res = await fetch(`${API_BASE}/students/department/${encodeURIComponent(dept)}`, { credentials: 'include' });
  return handleResponse(res);
}

export async function createStudent(studentData) {
  const res = await fetch(`${API_BASE}/students`, {
    method: 'POST', headers: getHeaders(), credentials: 'include',
    body: JSON.stringify(studentData),
  });
  return handleResponse(res);
}

// ===== FACULTY =====
export async function fetchFaculty() {
  const res = await fetch(`${API_BASE}/faculty`, { credentials: 'include' });
  return handleResponse(res);
}

// ===== ENROLLMENTS =====
export async function enrollInCourse(studentId, courseId) {
  const res = await fetch(`${API_BASE}/enrollments/enroll`, {
    method: 'POST', headers: getHeaders(), credentials: 'include',
    body: JSON.stringify({ studentId, courseId, academicYear: '2025-2026' }),
  });
  return handleResponse(res);
}

export async function fetchStudentEnrollments(studentId) {
  const res = await fetch(`${API_BASE}/enrollments/student/${studentId}`, { credentials: 'include' });
  return handleResponse(res);
}

export async function withdrawEnrollment(enrollmentId) {
  const res = await fetch(`${API_BASE}/enrollments/${enrollmentId}/withdraw`, {
    method: 'PUT', headers: getHeaders(), credentials: 'include',
  });
  return handleResponse(res);
}

// ===== GRADES =====
export async function fetchStudentGrades(studentId) {
  const res = await fetch(`${API_BASE}/grades/student/${studentId}`, { credentials: 'include' });
  return handleResponse(res);
}

export async function fetchStudentGPA(studentId) {
  const res = await fetch(`${API_BASE}/grades/student/${studentId}/gpa`, { credentials: 'include' });
  return handleResponse(res);
}

export async function fetchCourseGrades(courseId) {
  const res = await fetch(`${API_BASE}/grades/course/${courseId}`, { credentials: 'include' });
  return handleResponse(res);
}

export async function fetchEnrollmentGrade(enrollmentId) {
  const res = await fetch(`${API_BASE}/grades/enrollment/${enrollmentId}`, { credentials: 'include' });
  return handleResponse(res);
}

export async function addGrade(gradeData) {
  const res = await fetch(`${API_BASE}/grades`, {
    method: 'POST', headers: getHeaders(), credentials: 'include',
    body: JSON.stringify(gradeData),
  });
  return handleResponse(res);
}

export async function updateGrade(gradeId, gradeData) {
  const res = await fetch(`${API_BASE}/grades/${gradeId}`, {
    method: 'PUT', headers: getHeaders(), credentials: 'include',
    body: JSON.stringify(gradeData),
  });
  return handleResponse(res);
}

// ===== REPORTS =====
export async function fetchEnrollmentReport(courseId) {
  const res = await fetch(`${API_BASE}/reports/enrollment/${courseId}`, { credentials: 'include' });
  return handleResponse(res);
}

export async function fetchGradeDistribution(courseId) {
  const res = await fetch(`${API_BASE}/reports/grades/distribution/${courseId}`, { credentials: 'include' });
  return handleResponse(res);
}

export async function fetchFacultyWorkload(facultyId) {
  const res = await fetch(`${API_BASE}/reports/faculty/workload/${facultyId}`, { credentials: 'include' });
  return handleResponse(res);
}

export async function fetchDepartmentReport(dept) {
  const res = await fetch(`${API_BASE}/reports/department/${encodeURIComponent(dept)}`, { credentials: 'include' });
  return handleResponse(res);
}

export async function fetchStudentTranscript(studentId) {
  const res = await fetch(`${API_BASE}/reports/student/transcript/${studentId}`, { credentials: 'include' });
  return handleResponse(res);
}
