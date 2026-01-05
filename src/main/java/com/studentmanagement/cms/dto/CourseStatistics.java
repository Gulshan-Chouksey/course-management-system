package com.studentmanagement.cms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Course Statistics
 * Contains statistical information about a course including grades and enrollment
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseStatistics {
    
    private Long courseId;
    private String courseCode;
    private String courseName;
    private String facultyName;
    private String semester;
    
    // Enrollment statistics
    private int totalEnrolled;
    private int activeEnrollments;
    private int withdrawnEnrollments;
    private int completedEnrollments;
    
    // Grade statistics
    private int totalGraded;
    private double averageMarks;
    private double highestMarks;
    private double lowestMarks;
    private double passRate; // Percentage of students with passing grades
    
    // Grade distribution
    private int gradeA;
    private int gradeB;
    private int gradeC;
    private int gradeD;
    private int gradeF;
}
