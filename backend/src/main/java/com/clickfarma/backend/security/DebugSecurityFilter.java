package com.clickfarma.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class DebugSecurityFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {
        
        System.out.println("DEBUG: Request received - Method: " + request.getMethod() + 
                           ", Path: " + request.getRequestURI() + 
                           ", Origin: " + request.getHeader("Origin"));
        
        chain.doFilter(request, response);
    }
}
