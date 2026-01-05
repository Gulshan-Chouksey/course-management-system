package com.studentmanagement.cms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.studentmanagement.cms.entity.Enrollment;
import com.studentmanagement.cms.entity.Grade;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    // Find grade by enrollment
    Optional<Grade> findByEnrollment(Enrollment enrollment);

    // Find grades by grade value (e.g., A, B, C)
    List<Grade> findByGrade(String grade);

    // Find grades by total marks greater than a specific value
    List<Grade> findByTotalMarksGreaterThan(int marks);
    
}
