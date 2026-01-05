package com.studentmanagement.cms.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.studentmanagement.cms.entity.Student;
import com.studentmanagement.cms.entity.User;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    // Find student by User
    Optional<Student> findByUser(User user);
    
    // Find students by department
    List<Student> findByDepartment(String department);
    
    // Find students by first name containing (case-insensitive search)
    List<Student> findByFirstNameContaining(String name);
    
    // Find students by Enrollment Date Between startDate and endDate
    List<Student> findByEnrollmentDateBetween(LocalDate startDate, LocalDate endDate);
}
