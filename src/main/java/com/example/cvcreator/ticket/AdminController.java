package com.example.cvcreator.ticket;

import com.example.cvcreator.security.JwtUtil;
import com.example.cvcreator.user.User;
import com.example.cvcreator.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AdminController {

    private final TicketService ticketService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    private boolean isAdmin(HttpServletRequest request) {
        String username = getUsernameFromRequest(request);
        if (username == null) return false;

        return userRepository.findByUsername(username)
                .map(user -> user.getRole() == User.Role.ADMIN)
                .orElse(false);
    }

    private String getUsernameFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    try {
                        return jwtUtil.extractUsername(cookie.getValue());
                    } catch (Exception e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    @GetMapping("/tickets")
    public ResponseEntity<?> getAllTickets(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        List<Ticket> tickets = ticketService.getAllTickets();
        List<Map<String, Object>> response = tickets.stream()
                .map(this::mapTicketToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/tickets/stats")
    public ResponseEntity<?> getStats(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        return ResponseEntity.ok(ticketService.getStats());
    }

    @GetMapping("/tickets/{id}")
    public ResponseEntity<?> getTicket(@PathVariable UUID id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        return ticketService.getTicketById(id)
                .map(ticket -> ResponseEntity.ok(mapTicketToResponse(ticket)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/tickets/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable UUID id,
                                          @RequestBody Map<String, String> body,
                                          HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        String status = body.get("status");
        if (status == null || status.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Status is required"));
        }

        try {
            String enumStatus = status.toUpperCase().replace("-", "_");
            Ticket ticket = ticketService.updateStatus(id, enumStatus);
            return ResponseEntity.ok(mapTicketToResponse(ticket));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status: " + status));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/tickets/{id}/priority")
    public ResponseEntity<?> updatePriority(@PathVariable UUID id,
                                            @RequestBody Map<String, String> body,
                                            HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        String priority = body.get("priority");
        if (priority == null || priority.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Priority is required"));
        }

        try {
            String enumPriority = priority.toUpperCase();
            Ticket ticket = ticketService.updatePriority(id, enumPriority);
            return ResponseEntity.ok(mapTicketToResponse(ticket));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid priority: " + priority));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/tickets/{id}/responses")
    public ResponseEntity<?> addResponse(@PathVariable UUID id,
                                         @RequestBody TicketResponseDTO dto,
                                         HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        String username = getUsernameFromRequest(request);
        TicketResponse response = ticketService.addResponse(id, dto.getMessage(), username);
        return ResponseEntity.ok(mapResponseToDTO(response));
    }

    @DeleteMapping("/tickets/{id}")
    public ResponseEntity<?> deleteTicket(@PathVariable UUID id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        ticketService.deleteTicket(id);
        return ResponseEntity.ok(Map.of("message", "Ticket deleted"));
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        List<User> users = userRepository.findAll();
        List<Map<String, Object>> response = users.stream()
                .map(this::mapUserToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> mapTicketToResponse(Ticket ticket) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", ticket.getId().toString());
        map.put("subject", ticket.getSubject());
        map.put("message", ticket.getMessage());
        map.put("category", ticket.getCategory().name().toLowerCase());
        map.put("priority", ticket.getPriority().name().toLowerCase());
        map.put("status", ticket.getStatus().name().toLowerCase());
        map.put("userId", ticket.getUser().getId().toString());
        map.put("userName", ticket.getUser().getUsername());
        map.put("userEmail", ticket.getUser().getEmail());
        map.put("createdAt", ticket.getCreatedAt().toString());
        map.put("updatedAt", ticket.getUpdatedAt().toString());
        map.put("responses", ticket.getResponses().stream()
                .map(this::mapResponseToDTO)
                .collect(Collectors.toList()));
        return map;
    }

    private Map<String, Object> mapResponseToDTO(TicketResponse response) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", response.getId().toString());
        map.put("message", response.getMessage());
        map.put("authorId", response.getAuthor().getId().toString());
        map.put("authorName", response.getAuthor().getUsername());
        map.put("authorRole", response.getAuthor().getRole().name().toLowerCase());
        map.put("createdAt", response.getCreatedAt().toString());
        return map;
    }

    private Map<String, Object> mapUserToResponse(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId().toString());
        map.put("username", user.getUsername());
        map.put("email", user.getEmail());
        map.put("role", user.getRole().name());
        map.put("createdAt", user.getCreatedAt().toString());
        return map;
    }
}