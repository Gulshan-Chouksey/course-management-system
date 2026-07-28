package com.academiax.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.academiax.entity.Student;
import com.academiax.entity.User;
import com.academiax.repository.StudentRepository;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserService userService;

    public Student createStudent(Student student, User user) {
        if (student == null) {
            throw new IllegalArgumentException("Student details cannot be null");
        }
        if (user == null) {
            throw new IllegalArgumentException("User information cannot be null");
        }

        User createdUser = userService.createUser(user);
        student.setUser(createdUser);

        if (student.getFirstName() == null || student.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (student.getLastName() == null || student.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (student.getDepartment() == null || student.getDepartment().trim().isEmpty()) {
            throw new IllegalArgumentException("Department is required");
        }

        return studentRepository.save(student);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student getStudentById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid student ID");
        }

        Optional<Student> student = studentRepository.findById(id);
        if (student.isEmpty()) {
            throw new IllegalArgumentException("Student not found with ID: " + id);
        }

        return student.get();
    }

    public Student updateStudent(Long id, Student studentDetails) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid student ID");
        }

        Student existingStudent = getStudentById(id);

        if (studentDetails.getFirstName() != null && !studentDetails.getFirstName().trim().isEmpty()) {
            existingStudent.setFirstName(studentDetails.getFirstName());
        }

        if (studentDetails.getLastName() != null && !studentDetails.getLastName().trim().isEmpty()) {
            existingStudent.setLastName(studentDetails.getLastName());
        }

        if (studentDetails.getDateOfBirth() != null) {
            existingStudent.setDateOfBirth(studentDetails.getDateOfBirth());
        }

        if (studentDetails.getAddress() != null && !studentDetails.getAddress().trim().isEmpty()) {
            existingStudent.setAddress(studentDetails.getAddress());
        }

        if (studentDetails.getEnrollmentDate() != null) {
            existingStudent.setEnrollmentDate(studentDetails.getEnrollmentDate());
        }

        if (studentDetails.getDepartment() != null && !studentDetails.getDepartment().trim().isEmpty()) {
            existingStudent.setDepartment(studentDetails.getDepartment());
        }

        return studentRepository.save(existingStudent);
    }

    public void deleteStudent(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid student ID");
        }

        if (!studentRepository.existsById(id)) {
            throw new IllegalArgumentException("Student not found with ID: " + id);
        }

        studentRepository.deleteById(id);
    }

    public List<Student> searchStudents(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Search keyword cannot be null or empty");
        }

        return studentRepository.findByFirstNameContaining(keyword);
    }

    public List<Student> getStudentsByDepartment(String department) {
        if (department == null || department.trim().isEmpty()) {
            throw new IllegalArgumentException("Department name cannot be null or empty");
        }

        return studentRepository.findByDepartment(department);
    }
}
