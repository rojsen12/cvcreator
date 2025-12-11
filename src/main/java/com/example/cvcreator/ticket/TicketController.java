package com.example.cvcreator.ticket;

import com.example.cvcreator.security.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class TicketController {

    private final TicketService ticketService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<?> createTicket(@RequestBody TicketDTO dto, HttpServletRequest request) {
        String username = getUsernameFromRequest(request);
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        Ticket ticket = ticketService.createTicket(dto, username);
        return ResponseEntity.ok(mapTicketToResponse(ticket));
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyTickets(HttpServletRequest request) {
        String username = getUsernameFromRequest(request);
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        List<Ticket> tickets = ticketService.getUserTickets(username);
        List<Map<String, Object>> response = tickets.stream()
                .map(this::mapTicketToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTicket(@PathVariable UUID id, HttpServletRequest request) {
        String username = getUsernameFromRequest(request);
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        return ticketService.getTicketById(id)
                .map(ticket -> ResponseEntity.ok(mapTicketToResponse(ticket)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/responses")
    public ResponseEntity<?> addResponse(@PathVariable UUID id,
                                         @RequestBody TicketResponseDTO dto,
                                         HttpServletRequest request) {
        String username = getUsernameFromRequest(request);
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        TicketResponse response = ticketService.addResponse(id, dto.getMessage(), username);
        return ResponseEntity.ok(mapResponseToDTO(response));
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
}