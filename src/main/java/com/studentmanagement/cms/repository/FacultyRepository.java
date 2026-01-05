package com.studentmanagement.cms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.studentmanagement.cms.entity.Faculty;
import com.studentmanagement.cms.entity.User;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {

	// Find faculty by associated User
	Optional<Faculty> findByUser(User user);
	
	// Find faculty by username
	@Query("SELECT f FROM Faculty f WHERE f.user.username = :username")
	Optional<Faculty> findByUsername(@Param("username") String username);

	// Find all faculty in a given department
	List<Faculty> findByDepartment(String department);

	// Find faculty whose qualification contains the given string
	List<Faculty> findByQualificationContaining(String qualification);
}
