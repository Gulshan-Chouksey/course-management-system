package com.academiax.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.academiax.entity.Course;
import com.academiax.entity.Faculty;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

	// Find a course by its course code
	Optional<Course> findByCourseCode(String code);

	// Find all courses taught by a specific faculty member
	List<Course> findByFaculty(Faculty faculty);

	// Find all courses offered in a specific semester
	List<Course> findBySemester(String semester);

    // Search courses by name containing a specific keyword
    List<Course> findByCourseNameContaining(String name);

    // Check if a course exists by its course code
    boolean existsByCourseCode(String code);
}
