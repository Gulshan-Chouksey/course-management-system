package com.studentmanagement.cms.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.studentmanagement.cms.entity.Course;
import com.studentmanagement.cms.entity.Enrollment;
import com.studentmanagement.cms.service.EnrollmentService;

/**
 * Schedule Controller
 * Handles academic schedule viewing and management
 */
@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    @Autowired
    private EnrollmentService enrollmentService;

    /**
     * Get weekly schedule for a student
     * GET /api/schedule/student/{studentId}
     * 
     * @param studentId Student ID
     * @return ResponseEntity with student's weekly schedule
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'STUDENT')")
    public ResponseEntity<Map<String, Object>> getStudentSchedule(@PathVariable Long studentId) {
        // Get all active enrollments for the student
        List<Enrollment> enrollments = enrollmentService.getActiveEnrollments(studentId);
        
        // Extract courses from enrollments
        List<Course> courses = enrollments.stream()
            .map(Enrollment::getCourse)
            .collect(Collectors.toList());
        
        // Group courses by days of week
        Map<String, List<Course>> scheduleByDay = new HashMap<>();
        scheduleByDay.put("Monday", courses.stream()
            .filter(c -> c.getDaysOfWeek() != null && c.getDaysOfWeek().contains("M"))
            .collect(Collectors.toList()));
        scheduleByDay.put("Tuesday", courses.stream()
            .filter(c -> c.getDaysOfWeek() != null && c.getDaysOfWeek().contains("T"))
            .collect(Collectors.toList()));
        scheduleByDay.put("Wednesday", courses.stream()
            .filter(c -> c.getDaysOfWeek() != null && c.getDaysOfWeek().contains("W"))
            .collect(Collectors.toList()));
        scheduleByDay.put("Thursday", courses.stream()
            .filter(c -> c.getDaysOfWeek() != null && c.getDaysOfWeek().contains("Th"))
            .collect(Collectors.toList()));
        scheduleByDay.put("Friday", courses.stream()
            .filter(c -> c.getDaysOfWeek() != null && c.getDaysOfWeek().contains("F"))
            .collect(Collectors.toList()));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("studentId", studentId);
        response.put("totalCourses", courses.size());
        response.put("courses", courses);
        response.put("scheduleByDay", scheduleByDay);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get current student's weekly schedule
     * GET /api/schedule/my-schedule
     * 
     * @return ResponseEntity with current student's weekly schedule
     */
    @GetMapping("/my-schedule")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> getMySchedule() {
        // Get student ID from username (simplified - in production, use a service method)
        // For now, we'll return an error message to implement properly
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Please use /api/schedule/student/{studentId} endpoint");
        response.put("note", "Implementation requires student lookup by username");
        
        return ResponseEntity.ok(response);
    }
}
