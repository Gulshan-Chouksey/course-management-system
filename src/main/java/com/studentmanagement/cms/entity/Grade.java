package com.studentmanagement.cms.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name = "grades")
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gradeId;

    @OneToOne
    @JoinColumn(name = "enrollment_id", nullable = false)
    @NotNull
    private Enrollment enrollment;

    @Min(0)
    @Max(100)
    private int internalMarks;

    @Min(0)
    @Max(100)
    private int externalMarks;

    @Min(0)
    @Max(100)
    private int totalMarks;

    private String grade; // A, B, C, D, F

    private String remarks;

    private LocalDateTime entryDate;

}
