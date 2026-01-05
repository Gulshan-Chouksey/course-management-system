package com.studentmanagement.cms.entity;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long courseId;

    @NotBlank
    @Column(unique = true)
    private String courseCode;

    @NotBlank
    private String courseName;

    @NotNull
    @Min(1)
    @Max(6)
    private int credits;

    @NotNull
    private String description;

    @NotBlank
    private String semester;

    @ManyToOne
    private Faculty faculty;

    @NotNull
    @Min(1)
    private int maxCapacity;

    @NotNull
    @Min(0)
    private int currentEnrollment;

    // Academic Schedule Fields
    private LocalTime classStartTime;

    private LocalTime classEndTime;

    private String daysOfWeek; // e.g., "MWF" (Monday-Wednesday-Friday) or "TTh" (Tuesday-Thursday)

    private String roomNumber;

}
