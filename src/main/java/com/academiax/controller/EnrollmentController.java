package com.academiax.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.academiax.entity.Enrollment;
import com.academiax.service.EnrollmentService;

/**
 * Enrollment Controller
 * Handles enrollment operations with proper authorization checks
 * and meaningful error messages
 */
@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    /**
     * Enroll a student in a course
     * POST /api/enrollments/enroll
     * 
     * Validates:
     * - Course capacity
     * - Prerequisites
     * - Duplicate enrollment
     * 
     * @param requestBody Map containing studentId and courseId
     * @return ResponseEntity with enrollment details and success message
     */
    @PostMapping("/enroll")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<Map<String, Object>> enrollStudent(
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            Long studentId = ((Number) requestBody.get("studentId")).longValue();
            Long courseId = ((Number) requestBody.get("courseId")).longValue();
            String academicYear = (String) requestBody.getOrDefault("academicYear", "2025-2026");
            
            // Validate input
            if (studentId == null || courseId == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Both studentId and courseId are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Enroll student (service handles all validations)
            Enrollment enrollment = enrollmentService.enrollStudent(studentId, courseId, academicYear);
            
            // Create success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Student enrolled successfully");
            response.put("enrollment", enrollment);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            // Return meaningful error message
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            // Handle unexpected errors
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "An error occurred while processing enrollment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get all enrollments for a specific student
     * GET /api/enrollments/student/{studentId}
     * 
     * @param studentId Student ID
     * @return ResponseEntity with list of student's enrollments
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'STUDENT')")
    public ResponseEntity<Map<String, Object>> getStudentEnrollments(
            @PathVariable Long studentId) {
        
        try {
            List<Enrollment> enrollments = enrollmentService.getEnrollmentsByStudent(studentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", enrollments.size());
            response.put("enrollments", enrollments);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "An error occurred while retrieving enrollments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get all enrollments for a specific course (FACULTY access)
     * GET /api/enrollments/course/{courseId}
     * 
     * @param courseId Course ID
     * @return ResponseEntity with list of course enrollments
     */
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<Map<String, Object>> getCourseEnrollments(
            @PathVariable Long courseId) {
        
        try {
            List<Enrollment> enrollments = enrollmentService.getEnrollmentsByCourse(courseId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", enrollments.size());
            response.put("enrollments", enrollments);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "An error occurred while retrieving course enrollments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get enrollment details by enrollment ID
     * GET /api/enrollments/{id}
     * 
     * @param id Enrollment ID
     * @return ResponseEntity with enrollment details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'STUDENT')")
    public ResponseEntity<Map<String, Object>> getEnrollmentById(
            @PathVariable Long id) {
        
        try {
            Enrollment enrollment = enrollmentService.getEnrollmentById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("enrollment", enrollment);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "An error occurred while retrieving enrollment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Withdraw from a course
     * PUT /api/enrollments/{id}/withdraw
     * 
     * @param id Enrollment ID
     * @return ResponseEntity with success message
     */
    @PutMapping("/{id}/withdraw")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<Map<String, Object>> withdrawFromCourse(
            @PathVariable Long id) {
        
        try {
            enrollmentService.withdrawEnrollment(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully withdrawn from course");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "An error occurred while processing withdrawal: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get active enrollments for a specific student
     * GET /api/enrollments/active/student/{studentId}
     * 
     * @param studentId Student ID
     * @return ResponseEntity with list of active enrollments
     */
    @GetMapping("/active/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'STUDENT')")
    public ResponseEntity<Map<String, Object>> getActiveEnrollments(
            @PathVariable Long studentId) {
        
        try {
            List<Enrollment> enrollments = enrollmentService.getActiveEnrollments(studentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", enrollments.size());
            response.put("enrollments", enrollments);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "An error occurred while retrieving active enrollments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get enrollments by status
     * GET /api/enrollments/status/{status}
     * 
     * @param status Enrollment status (ACTIVE, WITHDRAWN, COMPLETED)
     * @return ResponseEntity with list of enrollments matching the status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<Map<String, Object>> getEnrollmentsByStatus(
            @PathVariable String status) {
        
        try {
            // Validate status
            if (!status.matches("^(ACTIVE|WITHDRAWN|COMPLETED)$")) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Invalid status. Must be one of: ACTIVE, WITHDRAWN, COMPLETED");
                return ResponseEntity.badRequest().body(response);
            }
            
            List<Enrollment> enrollments = enrollmentService.getEnrollmentsByStatus(status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("status", status);
            response.put("count", enrollments.size());
            response.put("enrollments", enrollments);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "An error occurred while retrieving enrollments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
