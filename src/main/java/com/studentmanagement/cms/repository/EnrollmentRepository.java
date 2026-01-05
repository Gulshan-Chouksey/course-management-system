package com.studentmanagement.cms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.studentmanagement.cms.entity.Course;
import com.studentmanagement.cms.entity.Enrollment;
import com.studentmanagement.cms.entity.Student;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

	List<Enrollment> findByStudent(Student student);

	List<Enrollment> findByCourse(Course course);

	Optional<Enrollment> findByStudentAndCourse(Student student, Course course);

	List<Enrollment> findByStatus(String status);

	int countByCourse(Course course);
}
