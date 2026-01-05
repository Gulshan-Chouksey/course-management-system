package com.studentmanagement.cms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * View Controller
 * Handles HTML page navigation and serving templates
 */
@Controller
public class ViewController {

    /**
     * Serve login page
     * GET /login
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Redirect root to login page
     * GET /
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    /**
     * Serve student dashboard
     * GET /student-dashboard
     */
    @GetMapping("/student-dashboard")
    public String studentDashboard() {
        return "student-dashboard";
    }

    /**
     * Serve faculty dashboard
     * GET /faculty-dashboard
     */
    @GetMapping("/faculty-dashboard")
    public String facultyDashboard() {
        return "faculty-dashboard";
    }

    /**
     * Serve admin dashboard
     * GET /admin-dashboard
     */
    @GetMapping("/admin-dashboard")
    public String adminDashboard() {
        return "admin-dashboard";
    }

    /**
     * Serve students management page
     * GET /students
     */
    @GetMapping("/students")
    public String students() {
        return "students";
    }

    /**
     * Serve courses management page
     * GET /courses
     */
    @GetMapping("/courses")
    public String courses() {
        return "courses";
    }

    /**
     * Serve reports page
     * GET /reports
     */
    @GetMapping("/reports")
    public String reports() {
        return "reports";
    }
}
