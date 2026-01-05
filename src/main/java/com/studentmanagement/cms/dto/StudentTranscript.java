package com.studentmanagement.cms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Student Transcript
 * Contains student information, all grades, and calculated GPA
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentTranscript {
    
    private Long studentId;
    private String studentName;
    private String department;
    private LocalDate enrollmentDate;
    
    private List<CourseGradeInfo> courses;
    
    private double gpa;
    private int totalCourses;
    private int totalCredits;
    private LocalDateTime generatedDate;
    
    /**
     * Inner class to represent course grade information in transcript
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseGradeInfo {
        private String courseCode;
        private String courseName;
        private int credits;
        private String semester;
        private int internalMarks;
        private int externalMarks;
        private int totalMarks;
        private String grade;
        private String remarks;
    }
}
