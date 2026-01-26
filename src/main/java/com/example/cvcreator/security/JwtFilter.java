package com.example.cvcreator.security;

import com.example.cvcreator.user.User;
import com.example.cvcreator.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    public JwtFilter(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        log.info("JWT Filter - Path: {}, Method: {}", path, method);

        if (path.startsWith("/api/users/register") ||
                path.startsWith("/api/users/login") ||
                method.equalsIgnoreCase("OPTIONS")) {
            log.debug("Skipping filter - public endpoint");
            filterChain.doFilter(request, response);
            return;
        }

        String token = null;

        if (request.getCookies() != null) {
            log.debug("Checking cookies");
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    token = cookie.getValue();
                    log.info("JWT token found in cookie");
                    break;
                }
            }
        } else {
            log.debug("No cookies in request");
        }

        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                log.info("JWT token found in Authorization header");
            }
        }

        if (token != null) {
            try {
                String username = jwtUtil.extractUsername(token);
                log.debug("Username from token: {}", username);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    Optional<User> userOpt = userService.getByUsername(username);

                    if (userOpt.isPresent()) {
                        User user = userOpt.get();

                        if (jwtUtil.validateToken(token, username)) {
                            log.info("Token is valid for user: {}", username);

                            List<SimpleGrantedAuthority> authorities = List.of(
                                    new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                            );

                            UsernamePasswordAuthenticationToken authToken =
                                    new UsernamePasswordAuthenticationToken(user, null, authorities);

                            SecurityContextHolder.getContext().setAuthentication(authToken);

                            log.info("Authentication set - User: {}, Role: {}", username, user.getRole().name());
                        } else {
                            log.warn("Token validation failed for user: {}", username);
                        }
                    } else {
                        log.warn("User not found in database: {}", username);
                    }
                } else if (SecurityContextHolder.getContext().getAuthentication() != null) {
                    log.debug("Authentication already exists");
                }
            } catch (Exception e) {
                log.error("Error processing token: {}", e.getMessage(), e);
            }
        } else {
            log.debug("No JWT token found in request");
        }

        filterChain.doFilter(request, response);
    }
}