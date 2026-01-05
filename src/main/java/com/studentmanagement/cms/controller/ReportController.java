package com.studentmanagement.cms.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.studentmanagement.cms.dto.CourseStatistics;
import com.studentmanagement.cms.dto.DepartmentReport;
import com.studentmanagement.cms.dto.StudentTranscript;
import com.studentmanagement.cms.entity.Course;
import com.studentmanagement.cms.entity.Student;
import com.studentmanagement.cms.service.ReportService;

/**
 * Report Controller
 * Provides comprehensive reporting endpoints with role-based access control
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * Get enrollment report for a course
     * GET /api/reports/enrollment/{courseId}
     * 
     * Returns list of students enrolled in the specified course
     * 
     * @param courseId Course ID
     * @return ResponseEntity with list of enrolled students
     */
    @GetMapping("/enrollment/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<Map<String, Object>> getEnrollmentReport(
            @PathVariable Long courseId) {
        
        try {
            List<Student> students = reportService.getEnrollmentReport(courseId);
            
            // Remove sensitive password information from user objects
            students.forEach(student -> {
                if (student.getUser() != null) {
                    student.getUser().setPassword(null);
                }
            });
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("courseId", courseId);
            response.put("totalStudents", students.size());
            response.put("students", students);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "An error occurred while generating enrollment report: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get grade distribution for a course
     * GET /api/reports/grades/distribution/{courseId}
     * 
     * Returns count of each grade (A, B, C, D, F) for the course
     * 
     * @param courseId Course ID
     * @return ResponseEntity with grade distribution map
     */
    @GetMapping("/grades/distribution/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<Map<String, Object>> getGradeDistribution(
            @PathVariable Long courseId) {
        
        try {
            Map<String, Integer> distribution = reportService.getGradeDistribution(courseId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("courseId", courseId);
            response.put("distribution", distribution);
            response.put("totalGraded", distribution.values().stream().mapToInt(Integer::intValue).sum());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "An error occurred while generating grade distribution: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get faculty workload report
     * GET /api/reports/faculty/workload/{facultyId}
     * 
     * Returns list of courses taught by the faculty member
     * 
     * @param facultyId Faculty ID
     * @return ResponseEntity with list of courses
     */
    @GetMapping("/faculty/workload/{facultyId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<Map<String, Object>> getFacultyWorkload(
            @PathVariable Long facultyId) {
        
        try {
            List<Course> courses = reportService.getFacultyWorkload(facultyId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("facultyId", facultyId);
            response.put("totalCourses", courses.size());
            response.put("courses", courses);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "An error occurred while generating faculty workload report: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Generate transcript for a student
     * GET /api/reports/student/transcript/{studentId}
     * 
     * Returns full academic transcript with all courses, grades, and GPA
     * 
     * @param studentId Student ID
     * @return ResponseEntity with student transcript
     */
    @GetMapping("/student/transcript/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'STUDENT')")
    public ResponseEntity<Map<String, Object>> getStudentTranscript(
            @PathVariable Long studentId) {
        
        try {
            StudentTranscript transcript = reportService.generateTranscript(studentId);
            
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
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get course statistics
     * GET /api/reports/course/statistics/{courseId}
     * 
     * Returns comprehensive statistics including average marks, pass rate,
     * grade distribution, enrollment stats, etc.
     * 
     * @param courseId Course ID
     * @return ResponseEntity with course statistics
     */
    @GetMapping("/course/statistics/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<Map<String, Object>> getCourseStatistics(
            @PathVariable Long courseId) {
        
        try {
            CourseStatistics statistics = reportService.getCourseStats(courseId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", statistics);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "An error occurred while generating course statistics: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get department report
     * GET /api/reports/department/{dept}
     * 
     * Returns comprehensive department statistics including student count,
     * faculty count, average GPA, pass rate, grade distribution, etc.
     * 
     * @param dept Department name
     * @return ResponseEntity with department report
     */
    @GetMapping("/department/{dept}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<Map<String, Object>> getDepartmentReport(
            @PathVariable String dept) {
        
        try {
            DepartmentReport report = reportService.getDepartmentReport(dept);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("report", report);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "An error occurred while generating department report: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
