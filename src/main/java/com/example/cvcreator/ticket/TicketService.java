package com.example.cvcreator.ticket;

import com.example.cvcreator.user.User;
import com.example.cvcreator.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketResponseRepository responseRepository;
    private final UserRepository userRepository;

    public Ticket createTicket(TicketDTO dto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Ticket ticket = new Ticket();
        ticket.setSubject(dto.getSubject());
        ticket.setMessage(dto.getMessage());
        ticket.setCategory(Ticket.Category.valueOf(dto.getCategory().toUpperCase()));
        ticket.setPriority(Ticket.Priority.valueOf(dto.getPriority().toUpperCase()));
        ticket.setUser(user);

        return ticketRepository.save(ticket);
    }

    public List<Ticket> getUserTickets(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ticketRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Ticket> getTicketById(UUID id) {
        return ticketRepository.findById(id);
    }

    public Ticket updateStatus(UUID ticketId, String status) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));

        try {
            Ticket.Status newStatus = Ticket.Status.valueOf(status.toUpperCase());
            ticket.setStatus(newStatus);
            return ticketRepository.save(ticket);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    public Ticket updatePriority(UUID ticketId, String priority) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));

        try {
            Ticket.Priority newPriority = Ticket.Priority.valueOf(priority.toUpperCase());
            ticket.setPriority(newPriority);
            return ticketRepository.save(ticket);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid priority: " + priority);
        }
    }

    public TicketResponse addResponse(UUID ticketId, String message, String username) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        TicketResponse response = new TicketResponse();
        response.setMessage(message);
        response.setTicket(ticket);
        response.setAuthor(author);

        if (author.getRole() == User.Role.ADMIN && ticket.getStatus() == Ticket.Status.OPEN) {
            ticket.setStatus(Ticket.Status.IN_PROGRESS);
            ticketRepository.save(ticket);
        }

        return responseRepository.save(response);
    }

    public void deleteTicket(UUID ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new RuntimeException("Ticket not found: " + ticketId);
        }
        ticketRepository.deleteById(ticketId);
    }

    public Map<String, Object> getStats() {
        long total = ticketRepository.count();
        long open = ticketRepository.countByStatus(Ticket.Status.OPEN);
        long inProgress = ticketRepository.countByStatus(Ticket.Status.IN_PROGRESS);
        long waiting = ticketRepository.countByStatus(Ticket.Status.WAITING);
        long resolved = ticketRepository.countByStatus(Ticket.Status.RESOLVED);
        long closed = ticketRepository.countByStatus(Ticket.Status.CLOSED);

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long todayNew = ticketRepository.countTodayTickets(startOfDay);

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("open", open);
        stats.put("inProgress", inProgress);
        stats.put("waiting", waiting);
        stats.put("resolved", resolved);
        stats.put("closed", closed);
        stats.put("todayNew", todayNew);
        stats.put("avgResponseTime", "2.5h");

        return stats;
    }
}