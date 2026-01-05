package com.studentmanagement.cms.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.studentmanagement.cms.entity.Course;
import com.studentmanagement.cms.entity.Enrollment;
import com.studentmanagement.cms.entity.Faculty;
import com.studentmanagement.cms.repository.CourseRepository;
import com.studentmanagement.cms.repository.EnrollmentRepository;
import com.studentmanagement.cms.repository.FacultyRepository;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    public Course createCourse(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("Course details cannot be null");
        }

        // Validate course code is unique
        if (course.getCourseCode() == null || course.getCourseCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Course code is required");
        }

        if (courseRepository.existsByCourseCode(course.getCourseCode())) {
            throw new IllegalArgumentException("Course code already exists: " + course.getCourseCode());
        }

        if (course.getCourseName() == null || course.getCourseName().trim().isEmpty()) {
            throw new IllegalArgumentException("Course name is required");
        }

        if (course.getCredits() < 1 || course.getCredits() > 6) {
            throw new IllegalArgumentException("Credits must be between 1 and 6");
        }

        if (course.getDescription() == null || course.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Description is required");
        }

        if (course.getSemester() == null || course.getSemester().trim().isEmpty()) {
            throw new IllegalArgumentException("Semester is required");
        }

        if (course.getMaxCapacity() < 1) {
            throw new IllegalArgumentException("Maximum capacity must be at least 1");
        }

        if (course.getCurrentEnrollment() < 0) {
            throw new IllegalArgumentException("Current enrollment cannot be negative");
        }

        if (course.getCurrentEnrollment() > course.getMaxCapacity()) {
            throw new IllegalArgumentException("Current enrollment cannot exceed maximum capacity");
        }

        return courseRepository.save(course);
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Course getCourseById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid course ID");
        }

        Optional<Course> course = courseRepository.findById(id);
        if (course.isEmpty()) {
            throw new IllegalArgumentException("Course not found with ID: " + id);
        }

        return course.get();
    }

    public Course getCourseByCourseCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Course code cannot be null or empty");
        }

        Optional<Course> course = courseRepository.findByCourseCode(code);
        if (course.isEmpty()) {
            throw new IllegalArgumentException("Course not found with code: " + code);
        }

        return course.get();
    }

    public Course updateCourse(Long id, Course courseDetails) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid course ID");
        }

        Course existingCourse = getCourseById(id);

        // Update fields if provided
        if (courseDetails.getCourseName() != null && !courseDetails.getCourseName().trim().isEmpty()) {
            existingCourse.setCourseName(courseDetails.getCourseName());
        }

        if (courseDetails.getCredits() > 0) {
            if (courseDetails.getCredits() < 1 || courseDetails.getCredits() > 6) {
                throw new IllegalArgumentException("Credits must be between 1 and 6");
            }
            existingCourse.setCredits(courseDetails.getCredits());
        }

        if (courseDetails.getDescription() != null && !courseDetails.getDescription().trim().isEmpty()) {
            existingCourse.setDescription(courseDetails.getDescription());
        }

        if (courseDetails.getSemester() != null && !courseDetails.getSemester().trim().isEmpty()) {
            existingCourse.setSemester(courseDetails.getSemester());
        }

        if (courseDetails.getMaxCapacity() > 0) {
            if (courseDetails.getMaxCapacity() < existingCourse.getCurrentEnrollment()) {
                throw new IllegalArgumentException("Maximum capacity cannot be less than current enrollment");
            }
            existingCourse.setMaxCapacity(courseDetails.getMaxCapacity());
        }

        if (courseDetails.getFaculty() != null) {
            existingCourse.setFaculty(courseDetails.getFaculty());
        }

        return courseRepository.save(existingCourse);
    }

    public void deleteCourse(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid course ID");
        }

        Course course = getCourseById(id);

        List<Enrollment> enrollments = enrollmentRepository.findByCourse(course);
        if (!enrollments.isEmpty()) {
            throw new IllegalArgumentException("Cannot delete course with existing enrollments");
        }

        courseRepository.deleteById(id);
    }

    public List<Course> getCoursesForCurrentFaculty() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("User is not authenticated");
        }
        
        String username = authentication.getName();
        
        Faculty faculty = facultyRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Faculty not found for user: " + username));
        
        return courseRepository.findByFaculty(faculty);
    }

    public List<Course> getCoursesByFaculty(Long facultyId) {
        if (facultyId == null || facultyId <= 0) {
            throw new IllegalArgumentException("Invalid faculty ID");
        }

        Faculty faculty = facultyRepository.findById(facultyId)
            .orElseThrow(() -> new IllegalArgumentException("Faculty not found with ID: " + facultyId));

        return courseRepository.findByFaculty(faculty);
    }

    public List<Course> getCoursesBySemester(String semester) {
        if (semester == null || semester.trim().isEmpty()) {
            throw new IllegalArgumentException("Semester cannot be null or empty");
        }

        return courseRepository.findBySemester(semester);
    }

    public List<Course> searchCourses(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Search keyword cannot be null or empty");
        }

        return courseRepository.findByCourseNameContaining(keyword);
    }

    public boolean isCourseCodeAvailable(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Course code cannot be null or empty");
        }

        return !courseRepository.existsByCourseCode(code);
    }
}
