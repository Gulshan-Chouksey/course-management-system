package com.studentmanagement.cms.config;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.studentmanagement.cms.entity.Course;
import com.studentmanagement.cms.entity.Enrollment;
import com.studentmanagement.cms.entity.Faculty;
import com.studentmanagement.cms.entity.Grade;
import com.studentmanagement.cms.entity.Student;
import com.studentmanagement.cms.entity.User;
import com.studentmanagement.cms.repository.CourseRepository;
import com.studentmanagement.cms.repository.EnrollmentRepository;
import com.studentmanagement.cms.repository.FacultyRepository;
import com.studentmanagement.cms.repository.GradeRepository;
import com.studentmanagement.cms.repository.StudentRepository;
import com.studentmanagement.cms.repository.UserRepository;

/**
 * Data Initializer
 * Populates the database with test data on application startup
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Check if data already exists
        if (courseRepository.count() > 0) {
            System.out.println("Database already contains data. Skipping initialization.");
            return;
        }

        System.out.println("Initializing test data...");

        // Create Admin Users
        createAdmin("admin@university.edu", "admin", "Admin@123");
        createAdmin("superadmin@university.edu", "superadmin", "Admin@123");

        // Create Faculty Members (let JPA generate IDs)
        Faculty faculty1 = createFaculty("John", "Smith", "Computer Science", "PhD", "john.smith@university.edu", "john.smith", "Faculty@123");
        Faculty faculty2 = createFaculty("Emily", "Johnson", "Computer Science", "PhD", "emily.johnson@university.edu", "emily.johnson", "Faculty@123");
        Faculty faculty3 = createFaculty("Michael", "Davis", "Mathematics", "PhD", "michael.davis@university.edu", "michael.davis", "Faculty@123");
        Faculty faculty4 = createFaculty("Sarah", "Williams", "Physics", "PhD", "sarah.williams@university.edu", "sarah.williams", "Faculty@123");
        Faculty faculty5 = createFaculty("David", "Brown", "Computer Science", "M.Tech", "david.brown@university.edu", "david.brown", "Faculty@123");

        // Create Student Members
        Student student1 = createStudent("Alice", "Anderson", "Computer Science", "alice.anderson@student.edu", "alice.anderson", "Student@123");
        Student student2 = createStudent("Bob", "Baker", "Computer Science", "bob.baker@student.edu", "bob.baker", "Student@123");
        Student student3 = createStudent("Charlie", "Clark", "Mathematics", "charlie.clark@student.edu", "charlie.clark", "Student@123");
        Student student4 = createStudent("Diana", "Davis", "Physics", "diana.davis@student.edu", "diana.davis", "Student@123");
        Student student5 = createStudent("Ethan", "Evans", "Computer Science", "ethan.evans@student.edu", "ethan.evans", "Student@123");

        // Create Courses
        createCourse("CS101", "Introduction to Programming", 3, 
                "Fundamentals of programming using Java. Covers basic syntax, control structures, and OOP concepts.",
                "Fall 2025", faculty1, 60, 0);

        createCourse("CS201", "Data Structures and Algorithms", 4,
                "Study of fundamental data structures and algorithms. Includes arrays, linked lists, trees, graphs, sorting, and searching.",
                "Fall 2025", faculty1, 50, 0);

        createCourse("CS301", "Database Management Systems", 3,
                "Relational database concepts, SQL, normalization, and transaction management.",
                "Spring 2026", faculty2, 45, 0);

        createCourse("CS401", "Web Development", 3,
                "Modern web development using HTML, CSS, JavaScript, and frameworks like React and Spring Boot.",
                "Fall 2025", faculty2, 40, 0);

        createCourse("CS501", "Machine Learning", 4,
                "Introduction to machine learning algorithms, supervised and unsupervised learning, neural networks.",
                "Spring 2026", faculty5, 35, 0);

        createCourse("MATH201", "Discrete Mathematics", 3,
                "Logic, sets, relations, functions, combinatorics, and graph theory for computer science.",
                "Fall 2025", faculty3, 55, 0);

        createCourse("MATH301", "Linear Algebra", 3,
                "Vector spaces, matrices, eigenvalues, eigenvectors, and their applications.",
                "Spring 2026", faculty3, 50, 0);

        createCourse("PHY101", "Physics for Engineers", 4,
                "Mechanics, thermodynamics, waves, and modern physics with engineering applications.",
                "Fall 2025", faculty4, 60, 0);

        createCourse("CS302", "Operating Systems", 4,
                "Process management, memory management, file systems, and concurrent programming.",
                "Spring 2026", faculty1, 45, 0);

        createCourse("CS402", "Computer Networks", 3,
                "Network protocols, TCP/IP, routing, switching, and network security fundamentals.",
                "Fall 2025", faculty5, 40, 0);

        // Get courses for enrollments
        Course cs101 = courseRepository.findByCourseCode("CS101").orElse(null);
        Course cs201 = courseRepository.findByCourseCode("CS201").orElse(null);
        Course cs301 = courseRepository.findByCourseCode("CS301").orElse(null);
        Course cs401 = courseRepository.findByCourseCode("CS401").orElse(null);
        Course math201 = courseRepository.findByCourseCode("MATH201").orElse(null);

        // Create Enrollments and Grades for Alice Anderson (Student 1)
        if (cs101 != null) {
            Enrollment e1 = createEnrollment(student1, cs101, "ACTIVE");
            createGrade(e1, 45, 48, "Excellent performance and consistent effort"); // Total: 93 (A)
        }
        if (cs201 != null) {
            Enrollment e2 = createEnrollment(student1, cs201, "ACTIVE");
            createGrade(e2, 40, 42, "Good understanding of data structures"); // Total: 82 (B)
        }
        if (math201 != null) {
            Enrollment e3 = createEnrollment(student1, math201, "ACTIVE");
            createGrade(e3, 42, 46, "Outstanding mathematical skills"); // Total: 88 (B)
        }
        if (cs401 != null) {
            Enrollment e4 = createEnrollment(student1, cs401, "ACTIVE");
            createGrade(e4, 44, 47, "Impressive web development project"); // Total: 91 (A)
        }

        // Create Enrollments and Grades for Bob Baker (Student 2)
        if (cs101 != null) {
            Enrollment e5 = createEnrollment(student2, cs101, "ACTIVE");
            createGrade(e5, 38, 40, "Good effort, needs improvement in debugging"); // Total: 78 (C)
        }
        if (cs301 != null) {
            Enrollment e6 = createEnrollment(student2, cs301, "ACTIVE");
            createGrade(e6, 41, 44, "Strong database design skills"); // Total: 85 (B)
        }
        if (cs201 != null) {
            Enrollment e7 = createEnrollment(student2, cs201, "ACTIVE");
            createGrade(e7, 36, 38, "Satisfactory performance"); // Total: 74 (C)
        }
        if (math201 != null) {
            Enrollment e8 = createEnrollment(student2, math201, "ACTIVE");
            createGrade(e8, 43, 45, "Excellent analytical thinking"); // Total: 88 (B)
        }

        // Create Enrollments and Grades for Charlie Clark (Student 3)
        if (cs101 != null) {
            Enrollment e9 = createEnrollment(student3, cs101, "ACTIVE");
            createGrade(e9, 47, 48, "Outstanding programming skills"); // Total: 95 (A)
        }
        if (cs401 != null) {
            Enrollment e10 = createEnrollment(student3, cs401, "ACTIVE");
            createGrade(e10, 45, 46, "Creative web design approach"); // Total: 91 (A)
        }
        if (math201 != null) {
            Enrollment e11 = createEnrollment(student3, math201, "ACTIVE");
            createGrade(e11, 48, 49, "Exceptional mathematical aptitude"); // Total: 97 (A)
        }
        if (cs301 != null) {
            Enrollment e12 = createEnrollment(student3, cs301, "ACTIVE");
            createGrade(e12, 42, 43, "Very good database optimization"); // Total: 85 (B)
        }

        // Create Enrollments and Grades for Diana Davis (Student 4)
        if (math201 != null) {
            Enrollment e13 = createEnrollment(student4, math201, "ACTIVE");
            createGrade(e13, 46, 48, "Exceptional performance in all areas"); // Total: 94 (A)
        }
        if (cs101 != null) {
            Enrollment e14 = createEnrollment(student4, cs101, "ACTIVE");
            createGrade(e14, 39, 41, "Good progress throughout semester"); // Total: 80 (B)
        }
        if (cs201 != null) {
            Enrollment e15 = createEnrollment(student4, cs201, "ACTIVE");
            createGrade(e15, 37, 39, "Solid understanding of algorithms"); // Total: 76 (C)
        }

        // Create Enrollments and Grades for Ethan Evans (Student 5)
        if (cs201 != null) {
            Enrollment e16 = createEnrollment(student5, cs201, "ACTIVE");
            createGrade(e16, 44, 46, "Excellent problem-solving skills"); // Total: 90 (A)
        }
        if (cs101 != null) {
            Enrollment e17 = createEnrollment(student5, cs101, "ACTIVE");
            createGrade(e17, 41, 42, "Very good coding practices"); // Total: 83 (B)
        }
        if (cs301 != null) {
            Enrollment e18 = createEnrollment(student5, cs301, "ACTIVE");
            createGrade(e18, 43, 44, "Strong SQL and database knowledge"); // Total: 87 (B)
        }
        if (cs401 != null) {
            Enrollment e19 = createEnrollment(student5, cs401, "ACTIVE");
            createGrade(e19, 46, 47, "Outstanding final project"); // Total: 93 (A)
        }

        System.out.println("Test data initialization completed!");
        System.out.println("- Created 2 admin users");
        System.out.println("- Created 5 faculty members");
        System.out.println("- Created 5 students");
        System.out.println("- Created 10 courses");
        System.out.println("- Created 19 enrollments with grades for all students");
        System.out.println("\nTest Credentials:");
        System.out.println("Admin usernames: admin, superadmin | Password: Admin@123");
        System.out.println("Faculty usernames: john.smith, emily.johnson, michael.davis, sarah.williams, david.brown | Password: Faculty@123");
        System.out.println("Student usernames: alice.anderson, bob.baker, charlie.clark, diana.davis, ethan.evans | Password: Student@123");
        System.out.println("\n=== SAMPLE GRADES CREATED FOR ALL STUDENTS ===");
        System.out.println("Alice Anderson: 4 courses with grades (GPA: ~3.5)");
        System.out.println("Bob Baker: 4 courses with grades (GPA: ~3.0)");
        System.out.println("Charlie Clark: 4 courses with grades (GPA: ~3.7)");
        System.out.println("Diana Davis: 3 courses with grades (GPA: ~3.3)");
        System.out.println("Ethan Evans: 4 courses with grades (GPA: ~3.5)");
    }

    /**
     * Creates an admin user
     */
    private void createAdmin(String email, String username, String password) {
        // Find or create User (idempotent)
        userRepository.findByUsername(username).orElseGet(() -> {
            User u = new User();
            u.setUsername(username);
            u.setEmail(email);
            u.setPassword(passwordEncoder.encode(password));
            u.setRole("ROLE_ADMIN");
            u.setPhone(generateRandomPhone());
            u.setCreatedDate(LocalDateTime.now());
            u.setIsActive(true);
            User savedUser = userRepository.save(u);
            System.out.println("Created admin user: " + username);
            return savedUser;
        });
    }

    /**
     * Creates a student with associated user account
     */
    private Student createStudent(String firstName, String lastName, String department,
                                   String email, String username, String password) {
        // Find or create User (idempotent)
        User user = userRepository.findByUsername(username).orElseGet(() -> {
            User u = new User();
            u.setUsername(username);
            u.setEmail(email);
            u.setPassword(passwordEncoder.encode(password));
            u.setRole("ROLE_STUDENT");
            u.setPhone(generateRandomPhone());
            u.setCreatedDate(LocalDateTime.now());
            u.setIsActive(true);
            User savedUser = userRepository.save(u);
            System.out.println("Created student user: " + username);
            return savedUser;
        });

        // Find or create Student (idempotent)
        return studentRepository.findByUser(user).orElseGet(() -> {
            Student student = new Student();
            student.setUser(user);
            student.setFirstName(firstName);
            student.setLastName(lastName);
            student.setDepartment(department);
            student.setEnrollmentDate(java.time.LocalDate.now().minusYears(new Random().nextInt(3)));
            student.setDateOfBirth(java.time.LocalDate.now().minusYears(20 + new Random().nextInt(5)));
            student.setAddress("Student Address " + new Random().nextInt(1000));
            Student savedStudent = studentRepository.save(student);
            System.out.println("Created student record: " + firstName + " " + lastName);
            return savedStudent;
        });
    }
    /**
     * Creates a faculty member with associated user account
     */
                private Faculty createFaculty(String firstName, String lastName, 
                                  String department, String qualification,
                                  String email, String username, String password) {
                        // Find or create User (idempotent)
                        User user = userRepository.findByUsername(username).orElseGet(() -> {
                                User u = new User();
                                u.setUsername(username);
                                u.setEmail(email);
                                u.setPassword(passwordEncoder.encode(password));
                                u.setRole("ROLE_FACULTY");
                                u.setPhone(generateRandomPhone());
                                u.setCreatedDate(LocalDateTime.now());
                                u.setIsActive(true);
                                return userRepository.save(u);
                        });

                        // Find or create Faculty (idempotent)
                        return facultyRepository.findByUser(user).orElseGet(() -> {
                                Faculty faculty = new Faculty();
                                faculty.setUser(user);
                                faculty.setFirstName(firstName);
                                faculty.setLastName(lastName);
                                faculty.setDepartment(department);
                                faculty.setQualification(qualification);
                                faculty.setJoiningDate(LocalDateTime.now().minusYears(new Random().nextInt(5)));
                                return facultyRepository.save(faculty);
                        });
    }

    /**
     * Creates a course
     */
        private void createCourse(String code, String name, int credits,
                             String description, String semester, Faculty faculty,
                             int maxCapacity, int currentEnrollment) {
                if (courseRepository.existsByCourseCode(code)) {
                        System.out.println("Course already exists: " + code + " - " + name + ". Skipping.");
                        return;
                }

                Course course = new Course();
                // Let the database generate the ID
                course.setCourseCode(code);
                course.setCourseName(name);
                course.setCredits(credits);
                course.setDescription(description);
                course.setSemester(semester);
                course.setFaculty(faculty);
                course.setMaxCapacity(maxCapacity);
                course.setCurrentEnrollment(currentEnrollment);

                courseRepository.save(course);
                System.out.println("Created course: " + code + " - " + name);
    }

    /**
     * Generates a random phone number
     */
    private String generateRandomPhone() {
        Random random = new Random();
        return String.format("+1-%03d-%03d-%04d", 
                random.nextInt(900) + 100,
                random.nextInt(900) + 100,
                random.nextInt(9000) + 1000);
    }

    /**
     * Creates an enrollment for a student in a course
     */
    private Enrollment createEnrollment(Student student, Course course, String status) {
        // Check if enrollment already exists
        java.util.Optional<Enrollment> existing = enrollmentRepository.findByStudentAndCourse(student, course);
        if (existing.isPresent()) {
            System.out.println("Enrollment already exists for student " + student.getFirstName() + " in course " + course.getCourseCode());
            return existing.get();
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrollmentDate(java.time.LocalDate.now().minusDays(new Random().nextInt(60)));
        enrollment.setStatus(status);
        enrollment.setAcademicYear("2025-2026"); // Set academic year
        
        Enrollment saved = enrollmentRepository.save(enrollment);
        
        // Update course enrollment count
        course.setCurrentEnrollment(course.getCurrentEnrollment() + 1);
        courseRepository.save(course);
        
        System.out.println("Created enrollment: " + student.getFirstName() + " " + student.getLastName() + " -> " + course.getCourseCode());
        return saved;
    }

    /**
     * Creates a grade for an enrollment
     */
    private Grade createGrade(Enrollment enrollment, int internalMarks, int externalMarks, String remarks) {
        // Check if grade already exists
        java.util.Optional<Grade> existing = gradeRepository.findByEnrollment(enrollment);
        if (existing.isPresent()) {
            System.out.println("Grade already exists for enrollment " + enrollment.getEnrollmentId());
            return existing.get();
        }

        Grade grade = new Grade();
        grade.setEnrollment(enrollment);
        grade.setInternalMarks(internalMarks);
        grade.setExternalMarks(externalMarks);
        grade.setTotalMarks(internalMarks + externalMarks);
        grade.setGrade(calculateLetterGrade(internalMarks + externalMarks));
        grade.setRemarks(remarks);
        grade.setEntryDate(java.time.LocalDateTime.now().minusDays(new Random().nextInt(30)));
        
        Grade saved = gradeRepository.save(grade);
        System.out.println("Created grade: " + grade.getGrade() + " (" + grade.getTotalMarks() + ") for " + 
                          enrollment.getStudent().getFirstName() + " in " + enrollment.getCourse().getCourseCode());
        return saved;
    }

    /**
     * Calculate letter grade based on total marks (out of 100)
     */
    private String calculateLetterGrade(int totalMarks) {
        if (totalMarks >= 90) return "A";
        else if (totalMarks >= 80) return "B";
        else if (totalMarks >= 70) return "C";
        else if (totalMarks >= 60) return "D";
        else return "F";
    }
}
