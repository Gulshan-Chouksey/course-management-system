package com.academiax.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.academiax.entity.Faculty;
import com.academiax.entity.User;
import com.academiax.repository.FacultyRepository;

@Service
public class FacultyService {

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private UserService userService;

    public Faculty createFaculty(Faculty faculty, User user) {
        if (faculty == null) {
            throw new IllegalArgumentException("Faculty details cannot be null");
        }
        if (user == null) {
            throw new IllegalArgumentException("User information cannot be null");
        }

        User createdUser = userService.createUser(user);
        faculty.setUser(createdUser);

        if (faculty.getFirstName() == null || faculty.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (faculty.getLastName() == null || faculty.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (faculty.getDepartment() == null || faculty.getDepartment().trim().isEmpty()) {
            throw new IllegalArgumentException("Department is required");
        }

        return facultyRepository.save(faculty);
    }

    public List<Faculty> getAllFaculty() {
        return facultyRepository.findAll();
    }

    public Faculty getFacultyById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid faculty ID");
        }

        Optional<Faculty> faculty = facultyRepository.findById(id);
        if (faculty.isEmpty()) {
            throw new IllegalArgumentException("Faculty not found with ID: " + id);
        }

        return faculty.get();
    }

    public Faculty updateFaculty(Long id, Faculty facultyDetails) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid faculty ID");
        }

        Faculty existingFaculty = getFacultyById(id);

        // Update fields if provided
        if (facultyDetails.getFirstName() != null && !facultyDetails.getFirstName().trim().isEmpty()) {
            existingFaculty.setFirstName(facultyDetails.getFirstName());
        }

        if (facultyDetails.getLastName() != null && !facultyDetails.getLastName().trim().isEmpty()) {
            existingFaculty.setLastName(facultyDetails.getLastName());
        }

        if (facultyDetails.getDepartment() != null && !facultyDetails.getDepartment().trim().isEmpty()) {
            existingFaculty.setDepartment(facultyDetails.getDepartment());
        }

        if (facultyDetails.getQualification() != null && !facultyDetails.getQualification().trim().isEmpty()) {
            existingFaculty.setQualification(facultyDetails.getQualification());
        }

        if (facultyDetails.getJoiningDate() != null) {
            existingFaculty.setJoiningDate(facultyDetails.getJoiningDate());
        }

        return facultyRepository.save(existingFaculty);
    }

    public void deleteFaculty(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid faculty ID");
        }

        if (!facultyRepository.existsById(id)) {
            throw new IllegalArgumentException("Faculty not found with ID: " + id);
        }

        facultyRepository.deleteById(id);
    }

    public List<Faculty> getFacultyByDepartment(String department) {
        if (department == null || department.trim().isEmpty()) {
            throw new IllegalArgumentException("Department name cannot be null or empty");
        }

        return facultyRepository.findByDepartment(department);
    }
}
