package com.studentmanagement.cms.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.studentmanagement.cms.entity.Enrollment;
import com.studentmanagement.cms.entity.Faculty;
import com.studentmanagement.cms.entity.Grade;
import com.studentmanagement.cms.entity.User;
import com.studentmanagement.cms.repository.EnrollmentRepository;
import com.studentmanagement.cms.repository.FacultyRepository;
import com.studentmanagement.cms.repository.GradeRepository;
import com.studentmanagement.cms.repository.UserRepository;

@Service
public class GradeService {

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    public Grade addGrade(Long enrollmentId, int internal, int external, String remarks) {
        if (enrollmentId == null || enrollmentId <= 0) {
            throw new IllegalArgumentException("Invalid enrollment ID");
        }

        if (internal < 0 || internal > 100) {
            throw new IllegalArgumentException("Internal marks must be between 0 and 100");
        }
        if (external < 0 || external > 100) {
            throw new IllegalArgumentException("External marks must be between 0 and 100");
        }

        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findById(enrollmentId);
        if (enrollmentOpt.isEmpty()) {
            throw new IllegalArgumentException("Enrollment not found with ID: " + enrollmentId);
        }

        Enrollment enrollment = enrollmentOpt.get();

        String currentUsername = SecurityContextHolder.getContext()
            .getAuthentication().getName();
        User user = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Faculty currentFaculty = facultyRepository.findByUser(user)
            .orElseThrow(() -> new IllegalArgumentException("Faculty not found for current user"));
        
        if (enrollment.getCourse() == null || enrollment.getCourse().getFaculty() == null) {
            throw new IllegalArgumentException("Course or faculty information missing");
        }
        
        if (!enrollment.getCourse().getFaculty().getFacultyId().equals(currentFaculty.getFacultyId())) {
            throw new IllegalArgumentException("You can only add grades for students in your own courses");
        }

        Optional<Grade> existingGrade = gradeRepository.findByEnrollment(enrollment);
        if (existingGrade.isPresent()) {
            throw new IllegalArgumentException("Grade already exists for this enrollment");
        }

        int totalMarks = internal + external;

        Grade grade = new Grade();
        grade.setEnrollment(enrollment);
        grade.setInternalMarks(internal);
        grade.setExternalMarks(external);
        grade.setTotalMarks(totalMarks);
        grade.setGrade(calculateLetterGrade(totalMarks));
        grade.setRemarks(remarks);
        grade.setEntryDate(LocalDateTime.now());

        return gradeRepository.save(grade);
    }

    public Grade updateGrade(Long gradeId, int internal, int external, String remarks) {
        if (gradeId == null || gradeId <= 0) {
            throw new IllegalArgumentException("Invalid grade ID");
        }

        if (internal < 0 || internal > 100) {
            throw new IllegalArgumentException("Internal marks must be between 0 and 100");
        }
        if (external < 0 || external > 100) {
            throw new IllegalArgumentException("External marks must be between 0 and 100");
        }

        Optional<Grade> gradeOpt = gradeRepository.findById(gradeId);
        if (gradeOpt.isEmpty()) {
            throw new IllegalArgumentException("Grade not found with ID: " + gradeId);
        }

        Grade existingGrade = gradeOpt.get();

        String currentUsername = SecurityContextHolder.getContext()
            .getAuthentication().getName();
        User user = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Faculty currentFaculty = facultyRepository.findByUser(user)
            .orElseThrow(() -> new IllegalArgumentException("Faculty not found for current user"));
        
        if (existingGrade.getEnrollment() == null || 
            existingGrade.getEnrollment().getCourse() == null || 
            existingGrade.getEnrollment().getCourse().getFaculty() == null) {
            throw new IllegalArgumentException("Course or faculty information missing");
        }
        
        if (!existingGrade.getEnrollment().getCourse().getFaculty().getFacultyId().equals(currentFaculty.getFacultyId())) {
            throw new IllegalArgumentException("You can only update grades for students in your own courses");
        }

        int totalMarks = internal + external;

        existingGrade.setInternalMarks(internal);
        existingGrade.setExternalMarks(external);
        existingGrade.setTotalMarks(totalMarks);
        existingGrade.setGrade(calculateLetterGrade(totalMarks));
        existingGrade.setRemarks(remarks);

        return gradeRepository.save(existingGrade);
    }

    public Grade getGradeByEnrollment(Long enrollmentId) {
        if (enrollmentId == null || enrollmentId <= 0) {
            throw new IllegalArgumentException("Invalid enrollment ID");
        }

        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findById(enrollmentId);
        if (enrollmentOpt.isEmpty()) {
            throw new IllegalArgumentException("Enrollment not found with ID: " + enrollmentId);
        }

        Enrollment enrollment = enrollmentOpt.get();

        Optional<Grade> gradeOpt = gradeRepository.findByEnrollment(enrollment);
        if (gradeOpt.isEmpty()) {
            throw new IllegalArgumentException("Grade not found for enrollment ID: " + enrollmentId);
        }

        return gradeOpt.get();
    }

    public List<Grade> getGradesByCourse(Long courseId) {
        if (courseId == null || courseId <= 0) {
            throw new IllegalArgumentException("Invalid course ID");
        }

        List<Enrollment> enrollments = enrollmentRepository.findAll().stream()
                .filter(e -> e.getCourse() != null && e.getCourse().getCourseId().equals(courseId))
                .toList();

        return gradeRepository.findAll().stream()
                .filter(g -> enrollments.contains(g.getEnrollment()))
                .toList();
    }

    public List<Grade> getGradesByStudent(Long studentId) {
        if (studentId == null || studentId <= 0) {
            throw new IllegalArgumentException("Invalid student ID");
        }

        List<Enrollment> enrollments = enrollmentRepository.findAll().stream()
                .filter(e -> e.getStudent() != null && e.getStudent().getStudentId().equals(studentId))
                .toList();

        return gradeRepository.findAll().stream()
                .filter(g -> enrollments.contains(g.getEnrollment()))
                .toList();
    }

    public String calculateLetterGrade(int totalMarks) {
        if (totalMarks >= 90 && totalMarks <= 200) {
            return "A";
        } else if (totalMarks >= 80 && totalMarks < 90) {
            return "B";
        } else if (totalMarks >= 70 && totalMarks < 80) {
            return "C";
        } else if (totalMarks >= 60 && totalMarks < 70) {
            return "D";
        } else {
            return "F";
        }
    }

    public double calculateGPA(List<Grade> grades) {
        if (grades == null || grades.isEmpty()) {
            return 0.0;
        }

        double totalGradePoints = 0.0;
        int count = 0;

        for (Grade grade : grades) {
            if (grade != null && grade.getGrade() != null) {
                double gradePoint = getGradePoint(grade.getGrade());
                totalGradePoints += gradePoint;
                count++;
            }
        }

        if (count == 0) {
            return 0.0;
        }

        return totalGradePoints / count;
    }

    private double getGradePoint(String letterGrade) {
        return switch (letterGrade) {
            case "A" -> 4.0;
            case "B" -> 3.0;
            case "C" -> 2.0;
            case "D" -> 1.0;
            case "F" -> 0.0;
            default -> 0.0;
        };
    }

    public double calculateStudentGPA(Long studentId) {
        List<Grade> grades = getGradesByStudent(studentId);
        return calculateGPA(grades);
    }

    public java.util.Map<String, Object> generateTranscript(Long studentId) {
        if (studentId == null || studentId <= 0) {
            throw new IllegalArgumentException("Invalid student ID");
        }

        List<Grade> grades = getGradesByStudent(studentId);
        double gpa = calculateGPA(grades);

        java.util.Map<String, Object> transcript = new java.util.HashMap<>();
        transcript.put("studentId", studentId);
        transcript.put("grades", grades);
        transcript.put("gpa", gpa);
        transcript.put("totalCourses", grades.size());
        transcript.put("generatedDate", LocalDateTime.now());

        return transcript;
    }

    public boolean isFacultyAuthorized(Long facultyId, Long enrollmentId) {
        try {
            if (facultyId == null || enrollmentId == null) {
                return false;
            }

            Optional<Enrollment> enrollmentOpt = enrollmentRepository.findById(enrollmentId);
            if (enrollmentOpt.isEmpty()) {
                return false;
            }

            Enrollment enrollment = enrollmentOpt.get();
            if (enrollment.getCourse() == null || enrollment.getCourse().getFaculty() == null) {
                return false;
            }

            return enrollment.getCourse().getFaculty().getFacultyId().equals(facultyId);
        } catch (Exception e) {
            return false;
        }
    }

    public List<Grade> getAllGrades() {
        return gradeRepository.findAll();
    }
}
