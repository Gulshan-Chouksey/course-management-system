package com.academiax.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.academiax.entity.Course;
import com.academiax.entity.Enrollment;
import com.academiax.entity.Student;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

	List<Enrollment> findByStudent(Student student);

	List<Enrollment> findByCourse(Course course);

	Optional<Enrollment> findByStudentAndCourse(Student student, Course course);

	List<Enrollment> findByStatus(String status);

	int countByCourse(Course course);
}
