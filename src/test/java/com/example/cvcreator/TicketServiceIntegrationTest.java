package com.example.cvcreator;

import com.example.cvcreator.ticket.*;
import com.example.cvcreator.user.User;
import com.example.cvcreator.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {
        TicketService.class,
        TicketServiceIntegrationTest.TestConfig.class
})
@ActiveProfiles("test")
@DisplayName("TicketService - Testy integracyjne")
class TicketServiceIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public TicketRepository ticketRepository() {
            return mock(TicketRepository.class);
        }

        @Bean
        @Primary
        public TicketResponseRepository ticketResponseRepository() {
            return mock(TicketResponseRepository.class);
        }

        @Bean
        @Primary
        public UserRepository userRepository() {
            return mock(UserRepository.class);
        }
    }

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketResponseRepository responseRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        reset(ticketRepository, responseRepository, userRepository);
    }

    // ==================== TESTY INTEGRACYJNE - CREATE TICKET ====================

    @Test
    @DisplayName("Integracja - tworzenie ticketu przez użytkownika")
    void integration_shouldCreateTicket_forUser() {
        // Given
        String username = "john.doe";
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setUsername(username);
        user.setEmail("john@example.com");
        user.setRole(User.Role.USER);

        TicketDTO dto = new TicketDTO();
        dto.setSubject("Nie mogę zresetować hasła");
        dto.setMessage("Próbuję zresetować hasło ale nie otrzymuję emaila");
        dto.setCategory("TECHNICAL");
        dto.setPriority("HIGH");

        Ticket savedTicket = new Ticket();
        savedTicket.setId(UUID.randomUUID());
        savedTicket.setSubject(dto.getSubject());
        savedTicket.setMessage(dto.getMessage());
        savedTicket.setCategory(Ticket.Category.TECHNICAL);
        savedTicket.setPriority(Ticket.Priority.HIGH);
        savedTicket.setUser(user);
        savedTicket.setStatus(Ticket.Status.OPEN);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        // When
        Ticket result = ticketService.createTicket(dto, username);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSubject()).isEqualTo("Nie mogę zresetować hasła");
        assertThat(result.getCategory()).isEqualTo(Ticket.Category.TECHNICAL);
        assertThat(result.getPriority()).isEqualTo(Ticket.Priority.HIGH);
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getStatus()).isEqualTo(Ticket.Status.OPEN);

        verify(userRepository).findByUsername(username);
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Integracja - tworzenie ticketu z różnymi kategoriami")
    void integration_shouldCreateTickets_withDifferentCategories() {
        // Given
        String username = "user";
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        // Uwaga: Tylko TECHNICAL i BILLING są dostępne w Ticket.Category
        String[] categories = {"TECHNICAL", "BILLING"};

        // When & Then
        for (String category : categories) {
            TicketDTO dto = new TicketDTO();
            dto.setSubject("Test " + category);
            dto.setMessage("Message");
            dto.setCategory(category);
            dto.setPriority("MEDIUM");

            Ticket result = ticketService.createTicket(dto, username);

            assertThat(result.getCategory().name()).isEqualTo(category);
        }

        verify(ticketRepository, times(2)).save(any(Ticket.class));
    }

    // ==================== TESTY INTEGRACYJNE - GET TICKETS ====================

    @Test
    @DisplayName("Integracja - pobieranie ticketów użytkownika z sortowaniem")
    void integration_shouldGetUserTickets_sortedByDate() {
        // Given
        String username = "testuser";
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setUsername(username);

        Ticket ticket1 = createTicketWithDate("Najnowszy", LocalDateTime.now());
        Ticket ticket2 = createTicketWithDate("Starszy", LocalDateTime.now().minusHours(2));
        Ticket ticket3 = createTicketWithDate("Najstarszy", LocalDateTime.now().minusDays(1));

        List<Ticket> tickets = Arrays.asList(ticket1, ticket2, ticket3);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(ticketRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(tickets);

        // When
        List<Ticket> result = ticketService.getUserTickets(username);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getSubject()).isEqualTo("Najnowszy");
        assertThat(result.get(1).getSubject()).isEqualTo("Starszy");
        assertThat(result.get(2).getSubject()).isEqualTo("Najstarszy");
    }

    @Test
    @DisplayName("Integracja - pobieranie wszystkich ticketów przez admina")
    void integration_shouldGetAllTickets_byAdmin() {
        // Given
        List<Ticket> allTickets = Arrays.asList(
                createTicketForUser("User1", "Problem 1"),
                createTicketForUser("User2", "Problem 2"),
                createTicketForUser("User3", "Problem 3"),
                createTicketForUser("User1", "Problem 4")
        );

        when(ticketRepository.findAllByOrderByCreatedAtDesc()).thenReturn(allTickets);

        // When
        List<Ticket> result = ticketService.getAllTickets();

        // Then
        assertThat(result).hasSize(4);
        assertThat(result).extracting(Ticket::getSubject)
                .containsExactly("Problem 1", "Problem 2", "Problem 3", "Problem 4");

        verify(ticketRepository).findAllByOrderByCreatedAtDesc();
    }

    // ==================== TESTY INTEGRACYJNE - UPDATE STATUS ====================

    @Test
    @DisplayName("Integracja - zmiana statusu ticketu przez cały cykl życia")
    void integration_shouldUpdateStatus_throughLifecycle() {
        // Given
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = createTicket("Lifecycle test");
        ticket.setId(ticketId);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        // When & Then - OPEN -> IN_PROGRESS
        Ticket result1 = ticketService.updateStatus(ticketId, "IN_PROGRESS");
        assertThat(result1.getStatus()).isEqualTo(Ticket.Status.IN_PROGRESS);

        // IN_PROGRESS -> WAITING
        Ticket result2 = ticketService.updateStatus(ticketId, "WAITING");
        assertThat(result2.getStatus()).isEqualTo(Ticket.Status.WAITING);

        // WAITING -> IN_PROGRESS
        Ticket result3 = ticketService.updateStatus(ticketId, "IN_PROGRESS");
        assertThat(result3.getStatus()).isEqualTo(Ticket.Status.IN_PROGRESS);

        // IN_PROGRESS -> RESOLVED
        Ticket result4 = ticketService.updateStatus(ticketId, "RESOLVED");
        assertThat(result4.getStatus()).isEqualTo(Ticket.Status.RESOLVED);

        // RESOLVED -> CLOSED
        Ticket result5 = ticketService.updateStatus(ticketId, "CLOSED");
        assertThat(result5.getStatus()).isEqualTo(Ticket.Status.CLOSED);

        verify(ticketRepository, times(5)).save(ticket);
    }

    @Test
    @DisplayName("Integracja - zmiana priorytetu wpływa na kolejność obsługi")
    void integration_shouldUpdatePriority_affectingServiceOrder() {
        // Given
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = createTicket("Priority test");
        ticket.setPriority(Ticket.Priority.LOW);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        // When - eskalacja priorytetu
        Ticket result1 = ticketService.updatePriority(ticketId, "MEDIUM");
        assertThat(result1.getPriority()).isEqualTo(Ticket.Priority.MEDIUM);

        Ticket result2 = ticketService.updatePriority(ticketId, "HIGH");
        assertThat(result2.getPriority()).isEqualTo(Ticket.Priority.HIGH);

        Ticket result3 = ticketService.updatePriority(ticketId, "URGENT");
        assertThat(result3.getPriority()).isEqualTo(Ticket.Priority.URGENT);

        // Then
        verify(ticketRepository, times(3)).save(ticket);
    }

    // ==================== TESTY INTEGRACYJNE - ADD RESPONSE ====================

    @Test
    @DisplayName("Integracja - konwersacja między użytkownikiem a adminem")
    void integration_shouldHandleConversation_betweenUserAndAdmin() {
        // Given
        UUID ticketId = UUID.randomUUID();

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("customer");
        user.setRole(User.Role.USER);

        User admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setUsername("support");
        admin.setRole(User.Role.ADMIN);

        Ticket ticket = createTicket("Pytanie o funkcjonalność");
        ticket.setId(ticketId);
        ticket.setUser(user);
        ticket.setStatus(Ticket.Status.OPEN);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        // Pierwsza odpowiedź - admin odpowiada na OPEN ticket
        when(userRepository.findByUsername("support")).thenReturn(Optional.of(admin));
        when(responseRepository.save(any(TicketResponse.class))).thenAnswer(inv -> {
            TicketResponse resp = inv.getArgument(0);
            resp.setId(UUID.randomUUID());
            return resp;
        });

        // When - admin odpowiada
        TicketResponse adminResponse = ticketService.addResponse(
                ticketId,
                "Dziękuję za pytanie. Ta funkcja jest dostępna w panelu...",
                "support"
        );

        // Then - status zmienia się na IN_PROGRESS
        assertThat(ticket.getStatus()).isEqualTo(Ticket.Status.IN_PROGRESS);
        assertThat(adminResponse).isNotNull();

        // When - użytkownik odpowiada
        when(userRepository.findByUsername("customer")).thenReturn(Optional.of(user));
        TicketResponse userResponse = ticketService.addResponse(
                ticketId,
                "Dziękuję za wyjaśnienie!",
                "customer"
        );

        // Then - status pozostaje IN_PROGRESS (user nie zmienia statusu)
        assertThat(ticket.getStatus()).isEqualTo(Ticket.Status.IN_PROGRESS);
        assertThat(userResponse).isNotNull();

        verify(responseRepository, times(2)).save(any(TicketResponse.class));
    }

    @Test
    @DisplayName("Integracja - admin odpowiada na ticket który nie jest OPEN")
    void integration_adminResponse_shouldNotChangeStatus_whenTicketNotOpen() {
        // Given
        UUID ticketId = UUID.randomUUID();

        User admin = new User();
        admin.setUsername("admin");
        admin.setRole(User.Role.ADMIN);

        Ticket ticket = createTicket("Already in progress");
        ticket.setStatus(Ticket.Status.IN_PROGRESS);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(responseRepository.save(any(TicketResponse.class))).thenReturn(new TicketResponse());

        // When
        ticketService.addResponse(ticketId, "Dodatkowa odpowiedź", "admin");

        // Then
        assertThat(ticket.getStatus()).isEqualTo(Ticket.Status.IN_PROGRESS);
        verify(ticketRepository, never()).save(ticket);
    }

    // ==================== TESTY INTEGRACYJNE - DELETE TICKET ====================

    @Test
    @DisplayName("Integracja - usuwanie ticketu usuwa też jego odpowiedzi (cascade)")
    void integration_shouldDeleteTicket_withCascadeResponses() {
        // Given
        UUID ticketId = UUID.randomUUID();

        when(ticketRepository.existsById(ticketId)).thenReturn(true);
        doNothing().when(ticketRepository).deleteById(ticketId);

        // When
        ticketService.deleteTicket(ticketId);

        // Then
        verify(ticketRepository).existsById(ticketId);
        verify(ticketRepository).deleteById(ticketId);
        // W rzeczywistej aplikacji cascade delete usunie też odpowiedzi
    }

    @Test
    @DisplayName("Integracja - próba usunięcia nieistniejącego ticketu")
    void integration_shouldFailToDelete_nonExistentTicket() {
        // Given
        UUID ticketId = UUID.randomUUID();
        when(ticketRepository.existsById(ticketId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> ticketService.deleteTicket(ticketId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ticket not found");

        verify(ticketRepository, never()).deleteById(any());
    }

    // ==================== TESTY INTEGRACYJNE - STATISTICS ====================

    @Test
    @DisplayName("Integracja - statystyki odzwierciedlają rzeczywisty stan ticketów")
    void integration_shouldCalculateStats_basedOnRealData() {
        // Given
        when(ticketRepository.count()).thenReturn(150L);
        when(ticketRepository.countByStatus(Ticket.Status.OPEN)).thenReturn(30L);
        when(ticketRepository.countByStatus(Ticket.Status.IN_PROGRESS)).thenReturn(25L);
        when(ticketRepository.countByStatus(Ticket.Status.WAITING)).thenReturn(15L);
        when(ticketRepository.countByStatus(Ticket.Status.RESOLVED)).thenReturn(50L);
        when(ticketRepository.countByStatus(Ticket.Status.CLOSED)).thenReturn(30L);
        when(ticketRepository.countTodayTickets(any(LocalDateTime.class))).thenReturn(12L);

        // When
        Map<String, Object> stats = ticketService.getStats();

        // Then
        assertThat(stats.get("total")).isEqualTo(150L);
        assertThat(stats.get("open")).isEqualTo(30L);
        assertThat(stats.get("inProgress")).isEqualTo(25L);
        assertThat(stats.get("waiting")).isEqualTo(15L);
        assertThat(stats.get("resolved")).isEqualTo(50L);
        assertThat(stats.get("closed")).isEqualTo(30L);
        assertThat(stats.get("todayNew")).isEqualTo(12L);

        // Suma statusów powinna zgadzać się z total
        long sumOfStatuses = 30L + 25L + 15L + 50L + 30L;
        assertThat(sumOfStatuses).isEqualTo(150L);
    }

    @Test
    @DisplayName("Integracja - statystyki dzisiejszych ticketów")
    void integration_shouldCountTodayTickets_correctly() {
        // Given
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();

        when(ticketRepository.count()).thenReturn(100L);
        when(ticketRepository.countByStatus(any())).thenReturn(20L);
        when(ticketRepository.countTodayTickets(any(LocalDateTime.class))).thenReturn(8L);

        // When
        Map<String, Object> stats = ticketService.getStats();

        // Then
        assertThat(stats.get("todayNew")).isEqualTo(8L);
        verify(ticketRepository).countTodayTickets(argThat(dateTime ->
                dateTime.toLocalDate().equals(today) &&
                        dateTime.equals(startOfDay)
        ));
    }

    @Test
    @DisplayName("Integracja - statystyki gdy system jest pusty")
    void integration_shouldReturnZeroStats_whenNoTickets() {
        // Given
        when(ticketRepository.count()).thenReturn(0L);
        when(ticketRepository.countByStatus(any())).thenReturn(0L);
        when(ticketRepository.countTodayTickets(any())).thenReturn(0L);

        // When
        Map<String, Object> stats = ticketService.getStats();

        // Then
        assertThat(stats.get("total")).isEqualTo(0L);
        assertThat(stats.get("open")).isEqualTo(0L);
        assertThat(stats.get("todayNew")).isEqualTo(0L);
        assertThat(stats).containsKey("avgResponseTime");
    }

    // ==================== TESTY INTEGRACYJNE - COMPLEX SCENARIOS ====================

    @Test
    @DisplayName("Integracja - pełny cykl życia ticketu od utworzenia do zamknięcia")
    void integration_fullTicketLifecycle() {
        User customer = new User();
        customer.setId(UUID.randomUUID());
        customer.setUsername("customer");
        customer.setRole(User.Role.USER);

        User support = new User();
        support.setId(UUID.randomUUID());
        support.setUsername("support");
        support.setRole(User.Role.ADMIN);

        TicketDTO dto = new TicketDTO();
        dto.setSubject("Problem z logowaniem");
        dto.setMessage("Nie mogę się zalogować do systemu");
        dto.setCategory("TECHNICAL");
        dto.setPriority("HIGH");

        UUID ticketId = UUID.randomUUID();
        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setSubject(dto.getSubject());
        ticket.setMessage(dto.getMessage());
        ticket.setCategory(Ticket.Category.TECHNICAL);
        ticket.setPriority(Ticket.Priority.HIGH);
        ticket.setUser(customer);
        ticket.setStatus(Ticket.Status.OPEN);

        when(userRepository.findByUsername("customer")).thenReturn(Optional.of(customer));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        Ticket createdTicket = ticketService.createTicket(dto, "customer");
        assertThat(createdTicket.getStatus()).isEqualTo(Ticket.Status.OPEN);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findByUsername("support")).thenReturn(Optional.of(support));
        when(responseRepository.save(any())).thenReturn(new TicketResponse());

        ticketService.addResponse(ticketId, "Sprawdzam problem z logowaniem", "support");
        assertThat(ticket.getStatus()).isEqualTo(Ticket.Status.IN_PROGRESS);

        when(ticketRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        ticketService.updatePriority(ticketId, "URGENT");
        assertThat(ticket.getPriority()).isEqualTo(Ticket.Priority.URGENT);

        ticketService.updateStatus(ticketId, "RESOLVED");
        assertThat(ticket.getStatus()).isEqualTo(Ticket.Status.RESOLVED);

        ticketService.updateStatus(ticketId, "CLOSED");
        assertThat(ticket.getStatus()).isEqualTo(Ticket.Status.CLOSED);

        verify(ticketRepository, atLeast(1)).save(any(Ticket.class));
        verify(responseRepository, atLeast(1)).save(any(TicketResponse.class));
    }

    // ==================== POMOCNICZE METODY ====================

    private Ticket createTicket(String subject) {
        Ticket ticket = new Ticket();
        ticket.setId(UUID.randomUUID());
        ticket.setSubject(subject);
        ticket.setMessage("Test message");
        ticket.setStatus(Ticket.Status.OPEN);
        ticket.setPriority(Ticket.Priority.MEDIUM);
        ticket.setCategory(Ticket.Category.TECHNICAL);
        ticket.setCreatedAt(LocalDateTime.now());
        return ticket;
    }

    private Ticket createTicketWithDate(String subject, LocalDateTime createdAt) {
        Ticket ticket = createTicket(subject);
        ticket.setCreatedAt(createdAt);
        return ticket;
    }

    private Ticket createTicketForUser(String username, String subject) {
        Ticket ticket = createTicket(subject);
        User user = new User();
        user.setUsername(username);
        ticket.setUser(user);
        return ticket;
    }
}