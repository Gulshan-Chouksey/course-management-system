package com.studentmanagement.cms.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studentmanagement.cms.entity.Course;
import com.studentmanagement.cms.entity.Enrollment;
import com.studentmanagement.cms.entity.Faculty;
import com.studentmanagement.cms.entity.Student;
import com.studentmanagement.cms.entity.User;
import com.studentmanagement.cms.repository.CourseRepository;
import com.studentmanagement.cms.repository.EnrollmentRepository;
import com.studentmanagement.cms.repository.FacultyRepository;
import com.studentmanagement.cms.repository.StudentRepository;
import com.studentmanagement.cms.repository.UserRepository;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    // Withdrawal deadline in days from enrollment date
    private static final int WITHDRAWAL_DEADLINE_DAYS = 14;

    /**
     * Enrolls a student in a course with comprehensive validation.
     * Checks:
     * - If student is already enrolled
     * - Course capacity
     * - Prerequisites (if any)
     * - Updates course enrollment count
     * 
     * @param studentId the student ID
     * @param courseId the course ID
     * @param academicYear the academic year (optional, auto-determined if null)
     * @return the created Enrollment entity
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public Enrollment enrollStudent(Long studentId, Long courseId, String academicYear) {
        // Validation: IDs must be valid
        if (studentId == null || studentId <= 0) {
            throw new IllegalArgumentException("Invalid student ID");
        }
        if (courseId == null || courseId <= 0) {
            throw new IllegalArgumentException("Invalid course ID");
        }

        // Verify student exists
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            throw new IllegalArgumentException("Student not found with ID: " + studentId);
        }
        Student student = studentOpt.get();

        // Verify course exists
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isEmpty()) {
            throw new IllegalArgumentException("Course not found with ID: " + courseId);
        }
        Course course = courseOpt.get();

        // Check if student is already enrolled in this course
        Optional<Enrollment> existingEnrollment = enrollmentRepository.findByStudentAndCourse(student, course);
        if (existingEnrollment.isPresent()) {
            Enrollment enrollment = existingEnrollment.get();
            if ("ACTIVE".equals(enrollment.getStatus())) {
                throw new IllegalArgumentException("Student is already enrolled in this course");
            } else if ("COMPLETED".equals(enrollment.getStatus())) {
                throw new IllegalArgumentException("Student has already completed this course");
            }
        }

        // Check course capacity
        if (course.getCurrentEnrollment() >= course.getMaxCapacity()) {
            throw new IllegalArgumentException("Course is full. Maximum capacity reached.");
        }

        // Check prerequisites (placeholder logic - can be enhanced)
        // In a real system, you would check if student has completed prerequisite courses
        // For now, we'll assume this validation passes

        // Create new enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrollmentDate(LocalDate.now());
        enrollment.setStatus("ACTIVE");
        // Use provided academicYear or auto-determine if not provided
        enrollment.setAcademicYear(academicYear != null && !academicYear.isBlank() ? academicYear : determineAcademicYear());

        // Update course enrollment count
        course.setCurrentEnrollment(course.getCurrentEnrollment() + 1);
        courseRepository.save(course);

        return enrollmentRepository.save(enrollment);
    }

    /**
     * Retrieves all enrollments for a specific student.
     * 
     * @param studentId the student ID
     * @return a list of enrollments for the student
     * @throws IllegalArgumentException if studentId is invalid or student not found
     */
    public List<Enrollment> getEnrollmentsByStudent(Long studentId) {
        // Validation: student ID must be valid
        if (studentId == null || studentId <= 0) {
            throw new IllegalArgumentException("Invalid student ID");
        }

        Optional<Student> student = studentRepository.findById(studentId);
        if (student.isEmpty()) {
            throw new IllegalArgumentException("Student not found with ID: " + studentId);
        }

        return enrollmentRepository.findByStudent(student.get());
    }

    /**
     * Retrieves all enrollments for a specific course.
     * 
     * @param courseId the course ID
     * @return a list of enrollments for the course
     * @throws IllegalArgumentException if courseId is invalid or course not found
     */
    public List<Enrollment> getEnrollmentsByCourse(Long courseId) {
        // Validation: course ID must be valid
        if (courseId == null || courseId <= 0) {
            throw new IllegalArgumentException("Invalid course ID");
        }

        Optional<Course> course = courseRepository.findById(courseId);
        if (course.isEmpty()) {
            throw new IllegalArgumentException("Course not found with ID: " + courseId);
        }

        return enrollmentRepository.findByCourse(course.get());
    }

    /**
     * Retrieves a specific enrollment by its ID.
     * 
     * @param id the enrollment ID
     * @return the Enrollment entity
     * @throws IllegalArgumentException if ID is invalid or enrollment not found
     */
    public Enrollment getEnrollmentById(Long id) {
        // Validation: ID must be valid
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid enrollment ID");
        }

        Optional<Enrollment> enrollment = enrollmentRepository.findById(id);
        if (enrollment.isEmpty()) {
            throw new IllegalArgumentException("Enrollment not found with ID: " + id);
        }

        return enrollment.get();
    }

    /**
     * Withdraws a student from a course.
     * Checks:
     * - Withdrawal deadline
     * - Updates status to WITHDRAWN
     * - Decreases course enrollment count
     * 
     * @param enrollmentId the enrollment ID
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public void withdrawEnrollment(Long enrollmentId) {
        // Validation: ID must be valid
        if (enrollmentId == null || enrollmentId <= 0) {
            throw new IllegalArgumentException("Invalid enrollment ID");
        }

        Enrollment enrollment = getEnrollmentById(enrollmentId);

        // Check if enrollment is already withdrawn or completed
        if ("WITHDRAWN".equals(enrollment.getStatus())) {
            throw new IllegalArgumentException("Enrollment is already withdrawn");
        }
        if ("COMPLETED".equals(enrollment.getStatus())) {
            throw new IllegalArgumentException("Cannot withdraw from a completed course");
        }

        // Check withdrawal deadline
        LocalDate enrollmentDate = enrollment.getEnrollmentDate();
        LocalDate deadline = enrollmentDate.plusDays(WITHDRAWAL_DEADLINE_DAYS);
        LocalDate today = LocalDate.now();

        if (today.isAfter(deadline)) {
            throw new IllegalArgumentException(
                "Withdrawal deadline has passed. Deadline was: " + deadline
            );
        }

        // Update enrollment status to WITHDRAWN
        enrollment.setStatus("WITHDRAWN");
        enrollmentRepository.save(enrollment);

        // Decrease course enrollment count
        Course course = enrollment.getCourse();
        if (course.getCurrentEnrollment() > 0) {
            course.setCurrentEnrollment(course.getCurrentEnrollment() - 1);
            courseRepository.save(course);
        }
    }

    /**
     * Retrieves all active enrollments for a specific student.
     * 
     * @param studentId the student ID
     * @return a list of active enrollments for the student
     * @throws IllegalArgumentException if studentId is invalid or student not found
     */
    public List<Enrollment> getActiveEnrollments(Long studentId) {
        // Validation: student ID must be valid
        if (studentId == null || studentId <= 0) {
            throw new IllegalArgumentException("Invalid student ID");
        }

        Optional<Student> student = studentRepository.findById(studentId);
        if (student.isEmpty()) {
            throw new IllegalArgumentException("Student not found with ID: " + studentId);
        }

        return enrollmentRepository.findByStudent(student.get())
                .stream()
                .filter(e -> "ACTIVE".equals(e.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Gets the current enrollment count for a specific course.
     * 
     * @param courseId the course ID
     * @return the number of active enrollments
     * @throws IllegalArgumentException if courseId is invalid or course not found
     */
    public int getEnrollmentCount(Long courseId) {
        // Validation: course ID must be valid
        if (courseId == null || courseId <= 0) {
            throw new IllegalArgumentException("Invalid course ID");
        }

        Optional<Course> course = courseRepository.findById(courseId);
        if (course.isEmpty()) {
            throw new IllegalArgumentException("Course not found with ID: " + courseId);
        }

        return enrollmentRepository.countByCourse(course.get());
    }

    /**
     * Helper method to determine the current academic year.
     * Format: "2024-2025" or "2025-2026"
     * 
     * @return the academic year string
     */
    private String determineAcademicYear() {
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        // If current month is August (8) or later, academic year is current-next
        // Otherwise, academic year is previous-current
        if (currentMonth >= 8) {
            return currentYear + "-" + (currentYear + 1);
        } else {
            return (currentYear - 1) + "-" + currentYear;
        }
    }

    /**
     * Checks if a student can enroll in a course.
     * Useful for pre-enrollment validation.
     * 
     * @param studentId the student ID
     * @param courseId the course ID
     * @return true if student can enroll, false otherwise
     */
    public boolean canEnroll(Long studentId, Long courseId) {
        try {
            // Validation: IDs must be valid
            if (studentId == null || studentId <= 0 || courseId == null || courseId <= 0) {
                return false;
            }

            Optional<Student> studentOpt = studentRepository.findById(studentId);
            Optional<Course> courseOpt = courseRepository.findById(courseId);

            if (studentOpt.isEmpty() || courseOpt.isEmpty()) {
                return false;
            }

            Student student = studentOpt.get();
            Course course = courseOpt.get();

            // Check if already enrolled
            Optional<Enrollment> existingEnrollment = enrollmentRepository.findByStudentAndCourse(student, course);
            if (existingEnrollment.isPresent() && 
                ("ACTIVE".equals(existingEnrollment.get().getStatus()) || 
                 "COMPLETED".equals(existingEnrollment.get().getStatus()))) {
                return false;
            }

            // Check course capacity and return accordingly
            return course.getCurrentEnrollment() < course.getMaxCapacity();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Retrieves enrollments by status.
     * 
     * @param status the enrollment status (ACTIVE, WITHDRAWN, COMPLETED)
     * @return a list of enrollments with the specified status
     */
    public List<Enrollment> getEnrollmentsByStatus(String status) {
        return enrollmentRepository.findByStatus(status);
    }

    /**
     * Retrieves all enrollments for courses taught by the currently logged-in faculty.
     * This method is used in the faculty dashboard to show only relevant enrollments.
     * 
     * @return a list of enrollments for the faculty's courses
     * @throws IllegalArgumentException if user is not authenticated or not a faculty member
     */
    public List<Enrollment> getEnrollmentsForFacultyCourses() {
        String currentUsername = SecurityContextHolder.getContext()
            .getAuthentication().getName();
        
        User user = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Faculty faculty = facultyRepository.findByUser(user)
            .orElseThrow(() -> new IllegalArgumentException("Faculty not found for current user"));
        
        List<Course> facultyCourses = courseRepository.findByFaculty(faculty);
        
        List<Enrollment> allEnrollments = new ArrayList<>();
        for (Course course : facultyCourses) {
            allEnrollments.addAll(enrollmentRepository.findByCourse(course));
        }
        
        return allEnrollments;
    }
}
