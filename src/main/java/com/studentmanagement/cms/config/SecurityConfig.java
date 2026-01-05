package com.studentmanagement.cms.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.studentmanagement.cms.entity.User;
import com.studentmanagement.cms.repository.UserRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;

    /**
     * Configure BCryptPasswordEncoder bean for password encoding
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configure UserDetailsService to load users from database
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername())
                    .password(user.getPassword())
                    .roles(user.getRole().replace("ROLE_", ""))
                    .build();
        };
    }

    /**
     * Configure HTTP Security with role-based access control
     * - H2 Console: Permit all access (development only)
     * - Login/Register endpoints: Permit all access
     * - ADMIN: Full access to all endpoints
     * - FACULTY: Access to course and grade management
     * - STUDENT: Access to view courses, enrollments, and grades (own data only)
     * - All other endpoints: Require authentication
     * - CSRF: Enabled with exceptions for H2 console (development)
     * - Security Headers: Enabled for XSS protection
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Permit all access to H2 console (DEVELOPMENT ONLY - disable in production)
                .requestMatchers("/h2-console/**").permitAll()
                
                // Permit access to static resources (CSS, JS, images)
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                
                // Permit access to login and register endpoints
                .requestMatchers("/", "/login", "/register", "/api/auth/**").permitAll()
                
                // Permit public access to view courses (GET only)
                .requestMatchers("/api/courses", "/api/courses/**").permitAll()
                
                // ADMIN: Full access to all endpoints
                .requestMatchers("/api/admin/**", "/admin/**").hasRole("ADMIN")
                
                // FACULTY: Access to grade management, view faculty, and students
                .requestMatchers("/api/grades/student/**").hasAnyRole("ADMIN", "FACULTY", "STUDENT")
                .requestMatchers("/courses/**", "/grades/**").hasAnyRole("ADMIN", "FACULTY")
                .requestMatchers("/api/faculty/**").hasAnyRole("ADMIN", "FACULTY")
                .requestMatchers("/students/**").hasAnyRole("ADMIN", "FACULTY")
                
                // STUDENT: Access to view students, enrollments
                .requestMatchers("/api/students/**", "/api/enrollments/**").hasAnyRole("ADMIN", "STUDENT", "FACULTY")
                .requestMatchers("/enrollments/**").hasAnyRole("ADMIN", "STUDENT")
                
                // Require authentication for all other endpoints
                .anyRequest().authenticated()
            )
            // CSRF Protection - Disabled only for H2 console in development
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**") // Allow H2 console access in development
            )
            
            // Configure security headers
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin()) // Allow frames from same origin (for H2 console)
                .xssProtection(xss -> xss
                    .headerValue(org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                ) // Enable XSS protection
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline';")
                )
            )
            
            // Configure form login
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(successHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            )

            
            // Configure logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            );
        
        return http.build();
    }
}
