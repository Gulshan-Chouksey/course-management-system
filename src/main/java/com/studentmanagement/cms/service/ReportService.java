package com.studentmanagement.cms.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.studentmanagement.cms.dto.CourseStatistics;
import com.studentmanagement.cms.dto.DepartmentReport;
import com.studentmanagement.cms.dto.StudentTranscript;
import com.studentmanagement.cms.entity.Course;
import com.studentmanagement.cms.entity.Enrollment;
import com.studentmanagement.cms.entity.Faculty;
import com.studentmanagement.cms.entity.Grade;
import com.studentmanagement.cms.entity.Student;
import com.studentmanagement.cms.repository.CourseRepository;
import com.studentmanagement.cms.repository.EnrollmentRepository;
import com.studentmanagement.cms.repository.FacultyRepository;
import com.studentmanagement.cms.repository.GradeRepository;
import com.studentmanagement.cms.repository.StudentRepository;

/**
 * Report Service
 * Provides comprehensive reporting functionality for the student management system
 */
@Service
public class ReportService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private GradeService gradeService;

    /**
     * Get enrollment report for a course
     * Returns list of students enrolled in the course
     * 
     * @param courseId the course ID
     * @return list of students enrolled in the course
     * @throws IllegalArgumentException if course ID is invalid or course not found
     */
    public List<Student> getEnrollmentReport(Long courseId) {
        // Validate course ID
        if (courseId == null || courseId <= 0) {
            throw new IllegalArgumentException("Invalid course ID");
        }

        // Find course
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isEmpty()) {
            throw new IllegalArgumentException("Course not found with ID: " + courseId);
        }

        Course course = courseOpt.get();

        // Get all enrollments for the course
        List<Enrollment> enrollments = enrollmentRepository.findByCourse(course);

        // Extract and return students
        return enrollments.stream()
                .map(Enrollment::getStudent)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Get grade distribution for a course
     * Returns count of each grade (A, B, C, D, F)
     * 
     * @param courseId the course ID
     * @return map with grade letters as keys and counts as values
     * @throws IllegalArgumentException if course ID is invalid or course not found
     */
    public Map<String, Integer> getGradeDistribution(Long courseId) {
        // Validate course ID
        if (courseId == null || courseId <= 0) {
            throw new IllegalArgumentException("Invalid course ID");
        }

        // Find course
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isEmpty()) {
            throw new IllegalArgumentException("Course not found with ID: " + courseId);
        }

        Course course = courseOpt.get();

        // Get all enrollments for the course
        List<Enrollment> enrollments = enrollmentRepository.findByCourse(course);

        // Initialize grade distribution map
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("A", 0);
        distribution.put("B", 0);
        distribution.put("C", 0);
        distribution.put("D", 0);
        distribution.put("F", 0);

        // Count grades for each enrollment
        for (Enrollment enrollment : enrollments) {
            Optional<Grade> gradeOpt = gradeRepository.findByEnrollment(enrollment);
            if (gradeOpt.isPresent()) {
                Grade grade = gradeOpt.get();
                String letterGrade = grade.getGrade();
                if (letterGrade != null && distribution.containsKey(letterGrade)) {
                    distribution.put(letterGrade, distribution.get(letterGrade) + 1);
                }
            }
        }

        return distribution;
    }

    /**
     * Get faculty workload report
     * Returns list of courses taught by a faculty member
     * 
     * @param facultyId the faculty ID
     * @return list of courses taught by the faculty
     * @throws IllegalArgumentException if faculty ID is invalid or faculty not found
     */
    public List<Course> getFacultyWorkload(Long facultyId) {
        // Validate faculty ID
        if (facultyId == null || facultyId <= 0) {
            throw new IllegalArgumentException("Invalid faculty ID");
        }

        // Find faculty
        Optional<Faculty> facultyOpt = facultyRepository.findById(facultyId);
        if (facultyOpt.isEmpty()) {
            throw new IllegalArgumentException("Faculty not found with ID: " + facultyId);
        }

        Faculty faculty = facultyOpt.get();

        // Get all courses taught by this faculty
        return courseRepository.findAll().stream()
                .filter(course -> course.getFaculty() != null &&
                        course.getFaculty().getFacultyId().equals(faculty.getFacultyId()))
                .collect(Collectors.toList());
    }

    /**
     * Generate full transcript for a student
     * Returns comprehensive transcript with all courses and grades
     * 
     * @param studentId the student ID
     * @return StudentTranscript DTO with complete information
     * @throws IllegalArgumentException if student ID is invalid or student not found
     */
    public StudentTranscript generateTranscript(Long studentId) {
        // Validate student ID
        if (studentId == null || studentId <= 0) {
            throw new IllegalArgumentException("Invalid student ID");
        }

        // Find student
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            throw new IllegalArgumentException("Student not found with ID: " + studentId);
        }

        Student student = studentOpt.get();

        // Get all enrollments for the student
        List<Enrollment> enrollments = enrollmentRepository.findByStudent(student);

        // Build course grade information list
        List<StudentTranscript.CourseGradeInfo> courseGrades = new ArrayList<>();
        int totalCredits = 0;

        for (Enrollment enrollment : enrollments) {
            Optional<Grade> gradeOpt = gradeRepository.findByEnrollment(enrollment);
            if (gradeOpt.isPresent()) {
                Grade grade = gradeOpt.get();
                Course course = enrollment.getCourse();

                StudentTranscript.CourseGradeInfo courseInfo = new StudentTranscript.CourseGradeInfo();
                courseInfo.setCourseCode(course.getCourseCode());
                courseInfo.setCourseName(course.getCourseName());
                courseInfo.setCredits(course.getCredits());
                courseInfo.setSemester(course.getSemester());
                courseInfo.setInternalMarks(grade.getInternalMarks());
                courseInfo.setExternalMarks(grade.getExternalMarks());
                courseInfo.setTotalMarks(grade.getTotalMarks());
                courseInfo.setGrade(grade.getGrade());
                courseInfo.setRemarks(grade.getRemarks());

                courseGrades.add(courseInfo);
                totalCredits += course.getCredits();
            }
        }

        // Calculate GPA
        double gpa = gradeService.calculateStudentGPA(studentId);

        // Build transcript
        StudentTranscript transcript = new StudentTranscript();
        transcript.setStudentId(studentId);
        transcript.setStudentName(student.getFirstName() + " " + student.getLastName());
        transcript.setDepartment(student.getDepartment());
        transcript.setEnrollmentDate(student.getEnrollmentDate());
        transcript.setCourses(courseGrades);
        transcript.setGpa(gpa);
        transcript.setTotalCourses(courseGrades.size());
        transcript.setTotalCredits(totalCredits);
        transcript.setGeneratedDate(LocalDateTime.now());

        return transcript;
    }

    /**
     * Get course statistics
     * Returns comprehensive statistics including average marks, pass rate, etc.
     * 
     * @param courseId the course ID
     * @return CourseStatistics DTO with detailed statistics
     * @throws IllegalArgumentException if course ID is invalid or course not found
     */
    public CourseStatistics getCourseStats(Long courseId) {
        // Validate course ID
        if (courseId == null || courseId <= 0) {
            throw new IllegalArgumentException("Invalid course ID");
        }

        // Find course
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isEmpty()) {
            throw new IllegalArgumentException("Course not found with ID: " + courseId);
        }

        Course course = courseOpt.get();

        // Get all enrollments for the course
        List<Enrollment> enrollments = enrollmentRepository.findByCourse(course);

        // Initialize statistics
        CourseStatistics stats = new CourseStatistics();
        stats.setCourseId(courseId);
        stats.setCourseCode(course.getCourseCode());
        stats.setCourseName(course.getCourseName());
        stats.setSemester(course.getSemester());

        // Set faculty name
        if (course.getFaculty() != null) {
            Faculty faculty = course.getFaculty();
            stats.setFacultyName(faculty.getFirstName() + " " + faculty.getLastName());
        } else {
            stats.setFacultyName("Not Assigned");
        }

        // Calculate enrollment statistics
        stats.setTotalEnrolled(enrollments.size());
        stats.setActiveEnrollments((int) enrollments.stream()
                .filter(e -> "ACTIVE".equals(e.getStatus())).count());
        stats.setWithdrawnEnrollments((int) enrollments.stream()
                .filter(e -> "WITHDRAWN".equals(e.getStatus())).count());
        stats.setCompletedEnrollments((int) enrollments.stream()
                .filter(e -> "COMPLETED".equals(e.getStatus())).count());

        // Get all grades for the course
        List<Grade> grades = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            Optional<Grade> gradeOpt = gradeRepository.findByEnrollment(enrollment);
            gradeOpt.ifPresent(grades::add);
        }

        stats.setTotalGraded(grades.size());

        // Calculate grade statistics
        if (!grades.isEmpty()) {
            // Calculate average, highest, and lowest marks
            double sum = grades.stream().mapToInt(Grade::getTotalMarks).sum();
            stats.setAverageMarks(sum / grades.size());
            stats.setHighestMarks(grades.stream().mapToInt(Grade::getTotalMarks).max().orElse(0));
            stats.setLowestMarks(grades.stream().mapToInt(Grade::getTotalMarks).min().orElse(0));

            // Calculate pass rate (grades A, B, C, D are passing, F is failing)
            long passingGrades = grades.stream()
                    .filter(g -> !"F".equals(g.getGrade())).count();
            stats.setPassRate((passingGrades * 100.0) / grades.size());

            // Calculate grade distribution
            stats.setGradeA((int) grades.stream().filter(g -> "A".equals(g.getGrade())).count());
            stats.setGradeB((int) grades.stream().filter(g -> "B".equals(g.getGrade())).count());
            stats.setGradeC((int) grades.stream().filter(g -> "C".equals(g.getGrade())).count());
            stats.setGradeD((int) grades.stream().filter(g -> "D".equals(g.getGrade())).count());
            stats.setGradeF((int) grades.stream().filter(g -> "F".equals(g.getGrade())).count());
        } else {
            stats.setAverageMarks(0.0);
            stats.setHighestMarks(0.0);
            stats.setLowestMarks(0.0);
            stats.setPassRate(0.0);
            stats.setGradeA(0);
            stats.setGradeB(0);
            stats.setGradeC(0);
            stats.setGradeD(0);
            stats.setGradeF(0);
        }

        return stats;
    }

    /**
     * Get department report
     * Returns comprehensive statistics for a department
     * 
     * @param department the department name
     * @return DepartmentReport DTO with detailed statistics
     * @throws IllegalArgumentException if department name is invalid
     */
    public DepartmentReport getDepartmentReport(String department) {
        // Validate department name
        if (department == null || department.trim().isEmpty()) {
            throw new IllegalArgumentException("Department name is required");
        }

        DepartmentReport report = new DepartmentReport();
        report.setDepartmentName(department);

        // Get all students in the department
        List<Student> students = studentRepository.findByDepartment(department);
        report.setTotalStudents(students.size());

        // Count active students (students with at least one active enrollment)
        int activeStudents = 0;
        double totalGPA = 0.0;
        int studentsAbove3_5 = 0;
        int studentsBelow2_0 = 0;

        for (Student student : students) {
            List<Enrollment> enrollments = enrollmentRepository.findByStudent(student);
            boolean hasActiveEnrollment = enrollments.stream()
                    .anyMatch(e -> "ACTIVE".equals(e.getStatus()));
            if (hasActiveEnrollment) {
                activeStudents++;
            }

            // Calculate GPA for each student
            try {
                double gpa = gradeService.calculateStudentGPA(student.getStudentId());
                totalGPA += gpa;
                if (gpa >= 3.5) studentsAbove3_5++;
                if (gpa < 2.0) studentsBelow2_0++;
            } catch (Exception e) {
                // Skip students without grades
            }
        }

        report.setActiveStudents(activeStudents);
        report.setAverageStudentGPA(students.isEmpty() ? 0.0 : totalGPA / students.size());
        report.setStudentsAbove3_5GPA(studentsAbove3_5);
        report.setStudentsBelow2_0GPA(studentsBelow2_0);

        // Get faculty count in the department
        List<Faculty> faculty = facultyRepository.findByDepartment(department);
        report.setTotalFaculty(faculty.size());

        // Get all courses in the department (courses taught by faculty in this department)
        List<Course> courses = courseRepository.findAll().stream()
                .filter(c -> c.getFaculty() != null && 
                        department.equals(c.getFaculty().getDepartment()))
                .collect(Collectors.toList());
        report.setTotalCourses(courses.size());

        // Calculate active enrollments across all department courses
        int totalActiveEnrollments = 0;
        for (Course course : courses) {
            List<Enrollment> enrollments = enrollmentRepository.findByCourse(course);
            totalActiveEnrollments += (int) enrollments.stream()
                    .filter(e -> "ACTIVE".equals(e.getStatus())).count();
        }
        report.setActiveEnrollments(totalActiveEnrollments);

        // Calculate performance statistics
        List<Grade> allGrades = new ArrayList<>();
        for (Course course : courses) {
            List<Enrollment> enrollments = enrollmentRepository.findByCourse(course);
            for (Enrollment enrollment : enrollments) {
                Optional<Grade> gradeOpt = gradeRepository.findByEnrollment(enrollment);
                gradeOpt.ifPresent(allGrades::add);
            }
        }

        if (!allGrades.isEmpty()) {
            // Calculate average marks
            double sumMarks = allGrades.stream().mapToInt(Grade::getTotalMarks).sum();
            report.setDepartmentAverageMarks(sumMarks / allGrades.size());

            // Calculate pass rate
            long passingGrades = allGrades.stream()
                    .filter(g -> !"F".equals(g.getGrade())).count();
            report.setPassRate((passingGrades * 100.0) / allGrades.size());

            // Calculate grade distribution
            report.setTotalGradeA((int) allGrades.stream().filter(g -> "A".equals(g.getGrade())).count());
            report.setTotalGradeB((int) allGrades.stream().filter(g -> "B".equals(g.getGrade())).count());
            report.setTotalGradeC((int) allGrades.stream().filter(g -> "C".equals(g.getGrade())).count());
            report.setTotalGradeD((int) allGrades.stream().filter(g -> "D".equals(g.getGrade())).count());
            report.setTotalGradeF((int) allGrades.stream().filter(g -> "F".equals(g.getGrade())).count());
        } else {
            report.setDepartmentAverageMarks(0.0);
            report.setPassRate(0.0);
            report.setTotalGradeA(0);
            report.setTotalGradeB(0);
            report.setTotalGradeC(0);
            report.setTotalGradeD(0);
            report.setTotalGradeF(0);
        }

        return report;
    }
}
