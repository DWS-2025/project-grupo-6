package es.xpressaly.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Component
public class PathTraversalFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestURI = URLDecoder.decode(request.getRequestURI(), StandardCharsets.UTF_8);
        String queryString = request.getQueryString();
        
        // Check for path traversal attempts in URI
        if (containsPathTraversalAttempt(requestURI)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path detected");
            return;
        }
        
        // Check query parameters if present
        if (queryString != null && containsPathTraversalAttempt(queryString)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid query parameters detected");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean containsPathTraversalAttempt(String input) {
        String normalized = input.replaceAll("\\\\", "/");
        return normalized.contains("../") || 
               normalized.contains("./") || 
               normalized.contains("..\\") || 
               normalized.contains(".\\") ||
               normalized.contains("~") ||
               normalized.contains("|") ||
               normalized.contains(">") ||
               normalized.contains("<") ||
               normalized.contains("*") ||
               normalized.contains("$") ||
               normalized.contains("!") ||
               normalized.contains("`");
    }
} 