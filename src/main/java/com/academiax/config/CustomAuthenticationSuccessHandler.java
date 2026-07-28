package com.academiax.config;

import java.io.IOException;
import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Custom Authentication Success Handler
 * Redirects users to appropriate dashboard based on their role
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        String redirectUrl = "/faculty-dashboard"; // default
        OUTER:        
        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            switch (role) {
                case "ROLE_ADMIN" -> {
                    redirectUrl = "/admin-dashboard";
                    break OUTER;
                }
                case "ROLE_FACULTY" -> {
                    redirectUrl = "/faculty-dashboard";
                    break OUTER;
                }
                case "ROLE_STUDENT" -> {
                    redirectUrl = "/student-dashboard";
                    break OUTER;
                }
                default -> {
                }
            }
        }
        
        response.sendRedirect(redirectUrl);
    }
}
