package com.studentmanagement.cms.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.studentmanagement.cms.entity.Course;
import com.studentmanagement.cms.entity.Enrollment;
import com.studentmanagement.cms.repository.EnrollmentRepository;
import com.studentmanagement.cms.service.CourseService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createCourse(@Valid @RequestBody Course course) {
        Course createdCourse = courseService.createCourse(course);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Course created successfully");
        response.put("course", createdCourse);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCourses() {
        List<Course> courses = courseService.getAllCourses();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", courses.size());
        response.put("courses", courses);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCourseById(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("course", course);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Map<String, Object>> getCourseByCourseCode(@PathVariable String code) {
        Course course = courseService.getCourseByCourseCode(code);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("course", course);
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody Course courseDetails) {
        
        Course updatedCourse = courseService.updateCourse(id, courseDetails);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Course updated successfully");
        response.put("course", updatedCourse);
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Course deleted successfully");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-courses")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<List<Course>> getMyCourses() {
        List<Course> courses = courseService.getCoursesForCurrentFaculty();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/faculty/{facultyId}")
    public ResponseEntity<Map<String, Object>> getCoursesByFaculty(@PathVariable Long facultyId) {
        List<Course> courses = courseService.getCoursesByFaculty(facultyId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("facultyId", facultyId);
        response.put("count", courses.size());
        response.put("courses", courses);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/semester/{semester}")
    public ResponseEntity<Map<String, Object>> getCoursesBySemester(@PathVariable String semester) {
        List<Course> courses = courseService.getCoursesBySemester(semester);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("semester", semester);
        response.put("count", courses.size());
        response.put("courses", courses);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchCourses(@RequestParam String keyword) {
        List<Course> courses = courseService.searchCourses(keyword);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("keyword", keyword);
        response.put("count", courses.size());
        response.put("courses", courses);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/enrollments")
    public ResponseEntity<Map<String, Object>> getCourseEnrollments(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        List<Enrollment> enrollments = enrollmentRepository.findByCourse(course);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("courseId", id);
        response.put("courseName", course.getCourseName());
        response.put("count", enrollments.size());
        response.put("enrollments", enrollments);
        
        return ResponseEntity.ok(response);
    }
}
