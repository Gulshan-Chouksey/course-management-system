package com.academiax.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Entity
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long enrollmentId;

    @ManyToOne
    private Student student;
    
    @ManyToOne
    private Course course;

    @NotNull
    private LocalDate enrollmentDate;

    @NotBlank
    @Pattern(regexp = "^(ACTIVE|WITHDRAWN|COMPLETED)$", message = "status must be one of ACTIVE, WITHDRAWN, COMPLETED")
    private String status;

    @NotBlank
    private String academicYear;
    
}
