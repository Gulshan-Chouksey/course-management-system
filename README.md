# Student Course Management System

A comprehensive **Student Course Management System** built with Spring Boot that allows educational institutions to manage students, courses, faculty, enrollments, grades, and schedules efficiently.

## 🚀 Features

- **Student Management** - Add, update, delete, and view student information
- **Course Management** - Create and manage courses with department associations
- **Faculty Management** - Manage faculty members and their course assignments
- **Enrollment System** - Enroll students in courses and track their progress
- **Grade Management** - Record and manage student grades
- **Schedule Management** - Create and manage course schedules
- **Reports** - Generate various academic reports
- **Authentication & Authorization** - Secure login with role-based access control (Admin, Faculty, Student)
- **Dashboard Views** - Dedicated dashboards for Admin, Faculty, and Students

## 🛠️ Technology Stack

| Technology | Description |
|------------|-------------|
| **Java 21** | Programming Language |
| **Spring Boot 3.5.7** | Application Framework |
| **Spring Security** | Authentication & Authorization |
| **Spring Data JPA** | Data Persistence |
| **Thymeleaf** | Server-side Template Engine |
| **H2 Database** | In-memory Database |
| **Lombok** | Boilerplate Code Reduction |
| **Maven** | Build Tool |

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/studentmanagement/cms/
│   │   ├── config/           # Security & Application Configuration
│   │   ├── controller/       # REST Controllers & View Controllers
│   │   ├── dto/              # Data Transfer Objects
│   │   ├── entity/           # JPA Entities
│   │   ├── exception/        # Custom Exceptions
│   │   ├── repository/       # Data Repositories
│   │   ├── service/          # Business Logic Services
│   │   └── validation/       # Custom Validators
│   └── resources/
│       ├── templates/        # Thymeleaf HTML Templates
│       ├── static/           # Static Resources (CSS, JS)
│       └── application.properties
└── test/                     # Unit & Integration Tests
```

## 🗃️ Entity Model

The system includes the following entities:

- **User** - Base entity for authentication
- **Student** - Student information and records
- **Faculty** - Faculty member details
- **Course** - Course information
- **Department** - Academic departments
- **Enrollment** - Student-Course enrollments
- **Grade** - Student grades
- **Semester** - Academic semesters

## 🚦 Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.6+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/Gulshan-Chouksey/course-management-system.git
   cd course-management-system
   ```

2. **Build the project**
   ```bash
   ./mvnw clean install
   ```

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Access the application**
   - Application: [http://localhost:8080](http://localhost:8080)
   - H2 Console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

### H2 Database Console

To access the H2 database console:
- **URL:** `http://localhost:8080/h2-console`
- **JDBC URL:** `jdbc:h2:mem:studentdb`
- **Username:** `user`
- **Password:** `password`

## 📡 API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/login` | Login page |
| POST | `/login` | Authenticate user |
| GET | `/logout` | Logout user |

### Students
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/students` | Get all students |
| GET | `/api/students/{id}` | Get student by ID |
| POST | `/api/students` | Create new student |
| PUT | `/api/students/{id}` | Update student |
| DELETE | `/api/students/{id}` | Delete student |

### Courses
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/courses` | Get all courses |
| GET | `/api/courses/{id}` | Get course by ID |
| POST | `/api/courses` | Create new course |
| PUT | `/api/courses/{id}` | Update course |
| DELETE | `/api/courses/{id}` | Delete course |

### Enrollments
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/enrollments` | Get all enrollments |
| POST | `/api/enrollments` | Create enrollment |
| DELETE | `/api/enrollments/{id}` | Delete enrollment |

### Grades
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/grades` | Get all grades |
| POST | `/api/grades` | Add/Update grade |

### Faculty
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/faculty` | Get all faculty |
| POST | `/api/faculty` | Create faculty |

### Reports
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/reports` | Generate reports |

## 🔐 Security

The application uses Spring Security with role-based access control:

- **ADMIN** - Full access to all features
- **FACULTY** - Access to courses, grades, and student information
- **STUDENT** - Access to personal information, enrollments, and grades

## 🧪 Testing

Run the tests using Maven:

```bash
./mvnw test
```

## 📮 Postman Collection

A Postman collection is included in the project root (`Student-Course-Management-System.postman_collection.json`) for testing the API endpoints.

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

This project was developed as part of a university academic requirement.

## 👤 Author

**Gulshan Chouksey**

- GitHub: [@Gulshan-Chouksey](https://github.com/Gulshan-Chouksey)

## 🤝 Contributing

Contributions, issues, and feature requests are welcome! Feel free to check the [issues page](https://github.com/Gulshan-Chouksey/course-management-system/issues).

## ⭐ Show your support

Give a ⭐️ if this project helped you!
