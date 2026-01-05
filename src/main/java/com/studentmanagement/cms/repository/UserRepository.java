package com.studentmanagement.cms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.studentmanagement.cms.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Find a user by their username
    Optional<User> findByUsername(String username);
    
    // Find a user by their email
    Optional<User> findByEmail(String email);
    
    // Check if a username already exists
    boolean existsByUsername(String username);
    
    // Check if an email already exists
    boolean existsByEmail(String email);
    
    // Find all users with a specific role
    List<User> findByRole(String role);
}
