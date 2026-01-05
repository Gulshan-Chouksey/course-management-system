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

import com.studentmanagement.cms.entity.Student;
import com.studentmanagement.cms.entity.User;
import com.studentmanagement.cms.service.StudentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createStudent(
            @Valid @RequestBody Map<String, Object> requestBody) {
        
        @SuppressWarnings("unchecked")
        Map<String, Object> studentData = (Map<String, Object>) requestBody.get("student");
        @SuppressWarnings("unchecked")
        Map<String, Object> userData = (Map<String, Object>) requestBody.get("user");
        
        User user = new User();
        user.setUsername((String) userData.get("username"));
        user.setEmail((String) userData.get("email"));
        user.setPassword((String) userData.get("password"));
        user.setRole((String) userData.get("role"));
        user.setPhone((String) userData.get("phone"));
        user.setCreatedDate(java.time.LocalDateTime.now());
        user.setIsActive(true);
        
        Student student = new Student();
        student.setFirstName((String) studentData.get("firstName"));
        student.setLastName((String) studentData.get("lastName"));
        student.setDateOfBirth(java.time.LocalDate.parse((String) studentData.get("dateOfBirth")));
        student.setAddress((String) studentData.get("address"));
        student.setEnrollmentDate(java.time.LocalDate.parse((String) studentData.get("enrollmentDate")));
        student.setDepartment((String) studentData.get("department"));
        
        Student createdStudent = studentService.createStudent(student, user);
        
        if (createdStudent.getUser() != null) {
            createdStudent.getUser().setPassword(null);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Student created successfully");
        response.put("student", createdStudent);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<Map<String, Object>> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        
        students.forEach(student -> {
            if (student.getUser() != null) {
                student.getUser().setPassword(null);
            }
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", students.size());
        response.put("students", students);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'STUDENT')")
    public ResponseEntity<Map<String, Object>> getStudentById(@PathVariable Long id) {
        Student student = studentService.getStudentById(id);
        
        if (student.getUser() != null) {
            student.getUser().setPassword(null);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("student", student);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> getCurrentStudent() {
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "User not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        String username = authentication.getName();
        
        // Search for student by username
        List<Student> students = studentService.getAllStudents();
        Student currentStudent = students.stream()
            .filter(s -> s.getUser() != null && s.getUser().getUsername().equals(username))
            .findFirst()
            .orElse(null);
        
        if (currentStudent == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Student profile not found for current user");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        if (currentStudent.getUser() != null) {
            currentStudent.getUser().setPassword(null);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("student", currentStudent);
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> updateMyProfile(@Valid @RequestBody Student updatedStudent) {
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "User not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        String username = authentication.getName();
        
        // Search for student by username
        List<Student> students = studentService.getAllStudents();
        Student currentStudent = students.stream()
            .filter(s -> s.getUser() != null && s.getUser().getUsername().equals(username))
            .findFirst()
            .orElse(null);
        
        if (currentStudent == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Student profile not found for current user");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        // Update only allowed fields (not ID, user, enrollmentDate, department)
        if (updatedStudent.getFirstName() != null) {
            currentStudent.setFirstName(updatedStudent.getFirstName());
        }
        if (updatedStudent.getLastName() != null) {
            currentStudent.setLastName(updatedStudent.getLastName());
        }
        if (updatedStudent.getAddress() != null) {
            currentStudent.setAddress(updatedStudent.getAddress());
        }
        if (updatedStudent.getDateOfBirth() != null) {
            currentStudent.setDateOfBirth(updatedStudent.getDateOfBirth());
        }
        
        // Save updated student using the service's update method
        Student saved = studentService.updateStudent(currentStudent.getStudentId(), currentStudent);
        
        // Remove password from response
        if (saved.getUser() != null) {
            saved.getUser().setPassword(null);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Profile updated successfully");
        response.put("student", saved);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update student (ADMIN only)
     * PUT /api/students/{id}
     * 
     * @param id Student ID
     * @param studentDetails Updated student details
     * @return ResponseEntity with updated student
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody Student studentDetails) {
        
        Student updatedStudent = studentService.updateStudent(id, studentDetails);
        
        // Remove password from response
        if (updatedStudent.getUser() != null) {
            updatedStudent.getUser().setPassword(null);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Student updated successfully");
        response.put("student", updatedStudent);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete student (ADMIN only)
     * DELETE /api/students/{id}
     * 
     * @param id Student ID
     * @return ResponseEntity with success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Student deleted successfully");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Search students by keyword
     * GET /api/students/search?keyword=
     * 
     * @param keyword Search keyword
     * @return ResponseEntity with list of matching students
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<Map<String, Object>> searchStudents(@RequestParam String keyword) {
        List<Student> students = studentService.searchStudents(keyword);
        
        // Remove passwords from response
        students.forEach(student -> {
            if (student.getUser() != null) {
                student.getUser().setPassword(null);
            }
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", students.size());
        response.put("keyword", keyword);
        response.put("students", students);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get students by department
     * GET /api/students/department/{dept}
     * 
     * @param dept Department name
     * @return ResponseEntity with list of students in the department
     */
    @GetMapping("/department/{dept}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<Map<String, Object>> getStudentsByDepartment(@PathVariable String dept) {
        List<Student> students = studentService.getStudentsByDepartment(dept);
        
        // Remove passwords from response
        students.forEach(student -> {
            if (student.getUser() != null) {
                student.getUser().setPassword(null);
            }
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("department", dept);
        response.put("count", students.size());
        response.put("students", students);
        
        return ResponseEntity.ok(response);
    }
}
