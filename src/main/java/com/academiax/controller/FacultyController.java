package com.academiax.controller;

import java.time.LocalDateTime;
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
import org.springframework.web.bind.annotation.RestController;

import com.academiax.entity.Faculty;
import com.academiax.entity.User;
import com.academiax.service.FacultyService;

import jakarta.validation.Valid;

/**
 * Faculty Controller
 * Handles CRUD operations for faculty with role-based access control
 */
@RestController
@RequestMapping("/api/faculty")
public class FacultyController {

    @Autowired
    private FacultyService facultyService;

    /**
     * Create a new faculty (ADMIN only)
     * POST /api/faculty
     * 
     * @param requestBody Map containing faculty and user details
     * @return ResponseEntity with created faculty and success message
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createFaculty(
            @Valid @RequestBody Map<String, Object> requestBody) {
        
        // Extract faculty and user from request body
        @SuppressWarnings("unchecked")
        Map<String, Object> facultyData = (Map<String, Object>) requestBody.get("faculty");
        @SuppressWarnings("unchecked")
        Map<String, Object> userData = (Map<String, Object>) requestBody.get("user");
        
        // Create User object
        User user = new User();
        user.setUsername((String) userData.get("username"));
        user.setEmail((String) userData.get("email"));
        user.setPassword((String) userData.get("password"));
        user.setRole((String) userData.get("role"));
        user.setPhone((String) userData.get("phone"));
        
        // Create Faculty object
        Faculty faculty = new Faculty();
        faculty.setFirstName((String) facultyData.get("firstName"));
        faculty.setLastName((String) facultyData.get("lastName"));
        faculty.setDepartment((String) facultyData.get("department"));
        faculty.setQualification((String) facultyData.get("qualification"));
        
        // Parse joiningDate
        if (facultyData.get("joiningDate") != null) {
            faculty.setJoiningDate(LocalDateTime.parse((String) facultyData.get("joiningDate")));
        }
        
        // Create faculty
        Faculty createdFaculty = facultyService.createFaculty(faculty, user);
        
        // Remove password from response
        if (createdFaculty.getUser() != null) {
            createdFaculty.getUser().setPassword(null);
        }
        
        // Create response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Faculty created successfully");
        response.put("faculty", createdFaculty);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all faculty members (ADMIN, FACULTY)
     * GET /api/faculty
     * 
     * @return ResponseEntity with list of all faculty members
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<Map<String, Object>> getAllFaculty() {
        List<Faculty> facultyList = facultyService.getAllFaculty();
        
        // Remove passwords from response
        facultyList.forEach(faculty -> {
            if (faculty.getUser() != null) {
                faculty.getUser().setPassword(null);
            }
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", facultyList.size());
        response.put("faculty", facultyList);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get faculty by ID
     * GET /api/faculty/{id}
     * 
     * @param id Faculty ID
     * @return ResponseEntity with faculty details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<Map<String, Object>> getFacultyById(@PathVariable Long id) {
        Faculty faculty = facultyService.getFacultyById(id);
        
        // Remove password from response
        if (faculty.getUser() != null) {
            faculty.getUser().setPassword(null);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("faculty", faculty);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update faculty (ADMIN only)
     * PUT /api/faculty/{id}
     * 
     * @param id Faculty ID
     * @param facultyDetails Updated faculty details
     * @return ResponseEntity with updated faculty
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateFaculty(
            @PathVariable Long id,
            @Valid @RequestBody Faculty facultyDetails) {
        
        Faculty updatedFaculty = facultyService.updateFaculty(id, facultyDetails);
        
        // Remove password from response
        if (updatedFaculty.getUser() != null) {
            updatedFaculty.getUser().setPassword(null);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Faculty updated successfully");
        response.put("faculty", updatedFaculty);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete faculty (ADMIN only)
     * DELETE /api/faculty/{id}
     * 
     * @param id Faculty ID
     * @return ResponseEntity with success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteFaculty(@PathVariable Long id) {
        facultyService.deleteFaculty(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Faculty deleted successfully");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get faculty by department
     * GET /api/faculty/department/{dept}
     * 
     * @param dept Department name
     * @return ResponseEntity with list of faculty in the department
     */
    @GetMapping("/department/{dept}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<Map<String, Object>> getFacultyByDepartment(@PathVariable String dept) {
        List<Faculty> facultyList = facultyService.getFacultyByDepartment(dept);
        
        // Remove passwords from response
        facultyList.forEach(faculty -> {
            if (faculty.getUser() != null) {
                faculty.getUser().setPassword(null);
            }
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("department", dept);
        response.put("count", facultyList.size());
        response.put("faculty", facultyList);
        
        return ResponseEntity.ok(response);
    }
}
