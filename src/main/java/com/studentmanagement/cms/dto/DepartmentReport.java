package com.studentmanagement.cms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Department Report
 * Contains comprehensive statistics about a department
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentReport {
    
    private String departmentName;
    
    // Student statistics
    private int totalStudents;
    private int activeStudents;
    private double averageStudentGPA;
    
    // Faculty statistics
    private int totalFaculty;
    
    // Course statistics
    private int totalCourses;
    private int activeEnrollments;
    
    // Performance statistics
    private double departmentAverageMarks;
    private double passRate;
    
    // Grade distribution across department
    private int totalGradeA;
    private int totalGradeB;
    private int totalGradeC;
    private int totalGradeD;
    private int totalGradeF;
    
    // Top performers
    private int studentsAbove3_5GPA;
    private int studentsBelow2_0GPA;
}
