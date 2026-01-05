package com.studentmanagement.cms.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.studentmanagement.cms.entity.Grade;
import com.studentmanagement.cms.entity.Student;
import com.studentmanagement.cms.entity.User;
import com.studentmanagement.cms.repository.StudentRepository;
import com.studentmanagement.cms.repository.UserRepository;
import com.studentmanagement.cms.service.GradeService;

/**
 * Grade Controller
 * Handles grade management operations with faculty authorization
 * and validation for marks (0-100)
 */
@RestController
@RequestMapping("/api/grades")
public class GradeController {

    @Autowired
    private GradeService gradeService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StudentRepository studentRepository;

    /**
     * Helper method to check if the current user has permission to access student data.
     * - ADMIN and FACULTY: Can access any student's data
     * - STUDENT: Can only access their own data
     * 
     * @param studentId The student ID being accessed
     * @return true if access is allowed, false otherwise
     */
    private boolean canAccessStudentData(Long studentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        // Get current user
        User currentUser = userRepository.findByUsername(username).orElse(null);
        if (currentUser == null) {
            return false;
        }
        
        // ADMIN and FACULTY can access any student's data
        if (currentUser.getRole().equals("ROLE_ADMIN") || currentUser.getRole().equals("ROLE_FACULTY")) {
            return true;
        }
        
        // STUDENT can only access their own data
        if (currentUser.getRole().equals("ROLE_STUDENT")) {
            Student currentStudent = studentRepository.findByUser(currentUser).orElse(null);
            if (currentStudent == null) {
                return false;
            }
            // Check if the student is accessing their own data
            return currentStudent.getStudentId().equals(studentId);
        }
        
        return false;
    }

    /**
     * Add a new grade (FACULTY only)
     * POST /api/grades
     * 
     * Request body: { enrollmentId, internalMarks, externalMarks, remarks }
     * Auto-calculates total and letter grade
     * Validates marks (0-100)
     * 
     * @param requestBody Map containing grade details
     * @return ResponseEntity with created grade and success message
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    public ResponseEntity<Map<String, Object>> addGrade(
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            // Extract and validate input
            if (!requestBody.containsKey("enrollmentId")) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "enrollmentId is required");
                return ResponseEntity.badRequest().body(response);
            }

            Long enrollmentId = ((Number) requestBody.get("enrollmentId")).longValue();
            
            // Validate and extract marks
            if (!requestBody.containsKey("internalMarks") || !requestBody.containsKey("externalMarks")) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Both internalMarks and externalMarks are required");
                return ResponseEntity.badRequest().body(response);
            }

            int internalMarks = ((Number) requestBody.get("internalMarks")).intValue();
            int externalMarks = ((Number) requestBody.get("externalMarks")).intValue();
            String remarks = (String) requestBody.get("remarks");

            // Validate marks range (0-100)
            if (internalMarks < 0 || internalMarks > 100) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Internal marks must be between 0 and 100");
                return ResponseEntity.badRequest().body(response);
            }

            if (externalMarks < 0 || externalMarks > 100) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "External marks must be between 0 and 100");
                return ResponseEntity.badRequest().body(response);
            }

            // Faculty can only grade their own courses
            // This would require getting the current authenticated faculty ID
            // and verifying they teach the course for this enrollment

            // Add grade (service handles validation)
            Grade grade = gradeService.addGrade(enrollmentId, internalMarks, externalMarks, remarks);
            
            // Create success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Grade added successfully");
            response.put("grade", grade);
            response.put("totalMarks", grade.getTotalMarks());
            response.put("letterGrade", grade.getGrade());
            
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
            response.put("message", "An error occurred while adding grade: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update an existing grade (FACULTY only)
     * PUT /api/grades/{id}
     * 
     * Request body: { internalMarks, externalMarks, remarks }
     * Auto-calculates total and letter grade
     * Validates marks (0-100)
     * 
     * @param id Grade ID
     * @param requestBody Map containing updated grade details
     * @return ResponseEntity with updated grade and success message
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    public ResponseEntity<Map<String, Object>> updateGrade(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            // Validate and extract marks
            if (!requestBody.containsKey("internalMarks") || !requestBody.containsKey("externalMarks")) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Both internalMarks and externalMarks are required");
                return ResponseEntity.badRequest().body(response);
            }

            int internalMarks = ((Number) requestBody.get("internalMarks")).intValue();
            int externalMarks = ((Number) requestBody.get("externalMarks")).intValue();
            String remarks = (String) requestBody.get("remarks");

            // Validate marks range (0-100)
            if (internalMarks < 0 || internalMarks > 100) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Internal marks must be between 0 and 100");
                return ResponseEntity.badRequest().body(response);
            }

            if (externalMarks < 0 || externalMarks > 100) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "External marks must be between 0 and 100");
                return ResponseEntity.badRequest().body(response);
            }

            // Faculty can only update grades for their own courses

            // Update grade (service handles validation)
            Grade grade = gradeService.updateGrade(id, internalMarks, externalMarks, remarks);
            
            // Create success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Grade updated successfully");
            response.put("grade", grade);
            response.put("totalMarks", grade.getTotalMarks());
            response.put("letterGrade", grade.getGrade());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "An error occurred while updating grade: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get grade for a specific enrollment
     * GET /api/grades/enrollment/{enrollmentId}
     * 
     * @param enrollmentId Enrollment ID
     * @return ResponseEntity with grade details
     */
    @GetMapping("/enrollment/{enrollmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'STUDENT')")
    public ResponseEntity<Map<String, Object>> getGradeByEnrollment(
            @PathVariable Long enrollmentId) {
        
        try {
            Grade grade = gradeService.getGradeByEnrollment(enrollmentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("grade", grade);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "An error occurred while retrieving grade: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get all grades for a specific student
     * GET /api/grades/student/{studentId}
     * 
     * SECURITY: Students can only access their own grades
     * ADMIN and FACULTY can access any student's grades
     * 
     * @param studentId Student ID
     * @return ResponseEntity with list of student's grades
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'STUDENT')")
    public ResponseEntity<Map<String, Object>> getGradesByStudent(
            @PathVariable Long studentId) {
        
        try {
            // Security check: Students can only access their own grades
            if (!canAccessStudentData(studentId)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Access denied. You can only view your own grades.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            List<Grade> grades = gradeService.getGradesByStudent(studentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", grades.size());
            response.put("grades", grades);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "An error occurred while retrieving grades: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get all grades for a specific course (FACULTY access)
     * GET /api/grades/course/{courseId}
     * 
     * @param courseId Course ID
     * @return ResponseEntity with list of course grades
     */
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<Map<String, Object>> getGradesByCourse(
            @PathVariable Long courseId) {
        
        try {
            List<Grade> grades = gradeService.getGradesByCourse(courseId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", grades.size());
            response.put("grades", grades);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "An error occurred while retrieving course grades: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Generate transcript for a student
     * GET /api/grades/student/{studentId}/transcript
     * 
     * Returns all grades with course details and calculated GPA
     * 
     * SECURITY: Students can only access their own transcript
     * ADMIN and FACULTY can access any student's transcript
     * 
     * @param studentId Student ID
     * @return ResponseEntity with transcript details
     */
    @GetMapping("/student/{studentId}/transcript")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'STUDENT')")
    public ResponseEntity<Map<String, Object>> generateTranscript(
            @PathVariable Long studentId) {
        
        try {
            // Security check: Students can only access their own transcript
            if (!canAccessStudentData(studentId)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Access denied. You can only view your own transcript.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            Map<String, Object> transcript = gradeService.generateTranscript(studentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("transcript", transcript);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "An error occurred while generating transcript: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Calculate GPA for a student
     * GET /api/grades/student/{studentId}/gpa
     * 
     * Calculates and returns the GPA (0.0-4.0)
     * 
     * SECURITY: Students can only access their own GPA
     * ADMIN and FACULTY can access any student's GPA
     * 
     * @param studentId Student ID
     * @return ResponseEntity with calculated GPA
     */
    @GetMapping("/student/{studentId}/gpa")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'STUDENT')")
    public ResponseEntity<Map<String, Object>> calculateGPA(
            @PathVariable Long studentId) {
        
        try {
            // Security check: Students can only access their own GPA
            if (!canAccessStudentData(studentId)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Access denied. You can only view your own GPA.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            double gpa = gradeService.calculateStudentGPA(studentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("gpa", gpa);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "An error occurred while calculating GPA: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * DEBUG: Get all grades in the system (ADMIN only)
     * GET /api/grades/all
     * 
     * @return ResponseEntity with all grades
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllGrades() {
        try {
            List<Grade> allGrades = gradeService.getAllGrades();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", allGrades.size());
            response.put("grades", allGrades);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
