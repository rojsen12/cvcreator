package com.example.cvcreator;

import com.example.cvcreator.ticket.*;
import com.example.cvcreator.user.User;
import com.example.cvcreator.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TicketService - Testy jednostkowe")
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketResponseRepository responseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TicketService ticketService;

    // ==================== TESTY createTicket ====================

    @Test
    @DisplayName("createTicket - powinien utworzyć ticket z poprawnymi danymi")
    void createTicket_shouldCreateTicket_withCorrectData() {
        // Given
        String username = "john.doe";
        TicketDTO dto = new TicketDTO();
        dto.setSubject("Problem z logowaniem");
        dto.setMessage("Nie mogę się zalogować");
        dto.setCategory("TECHNICAL");
        dto.setPriority("HIGH");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(username);

        Ticket savedTicket = new Ticket();
        savedTicket.setId(UUID.randomUUID());
        savedTicket.setSubject(dto.getSubject());
        savedTicket.setMessage(dto.getMessage());
        savedTicket.setUser(user);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        // When
        Ticket result = ticketService.createTicket(dto, username);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSubject()).isEqualTo("Problem z logowaniem");
        assertThat(result.getMessage()).isEqualTo("Nie mogę się zalogować");
        verify(userRepository).findByUsername(username);
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    @DisplayName("createTicket - powinien rzucić wyjątek gdy użytkownik nie istnieje")
    void createTicket_shouldThrowException_whenUserNotFound() {
        // Given
        String username = "nonexistent";
        TicketDTO dto = new TicketDTO();
        dto.setSubject("Test");
        dto.setMessage("Test message");
        dto.setCategory("TECHNICAL");
        dto.setPriority("LOW");

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ticketService.createTicket(dto, username))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");

        verify(userRepository).findByUsername(username);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("createTicket - powinien poprawnie ustawić category z enum")
    void createTicket_shouldSetCategory_fromEnum() {
        // Given
        String username = "user";
        TicketDTO dto = new TicketDTO();
        dto.setSubject("Subject");
        dto.setMessage("Message");
        dto.setCategory("BILLING");
        dto.setPriority("MEDIUM");

        User user = new User();
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Ticket result = ticketService.createTicket(dto, username);

        // Then
        verify(ticketRepository).save(argThat(ticket ->
                ticket.getCategory() == Ticket.Category.BILLING
        ));
    }

    @Test
    @DisplayName("createTicket - powinien poprawnie ustawić priority z enum")
    void createTicket_shouldSetPriority_fromEnum() {
        // Given
        String username = "user";
        TicketDTO dto = new TicketDTO();
        dto.setSubject("Subject");
        dto.setMessage("Message");
        dto.setCategory("TECHNICAL");
        dto.setPriority("URGENT");

        User user = new User();
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ticketService.createTicket(dto, username);

        // Then
        verify(ticketRepository).save(argThat(ticket ->
                ticket.getPriority() == Ticket.Priority.URGENT
        ));
    }

    @Test
    @DisplayName("createTicket - powinien zapisać ticket z przypisanym użytkownikiem")
    void createTicket_shouldSaveTicket_withAssignedUser() {
        // Given
        String username = "admin";
        UUID userId = UUID.randomUUID();
        TicketDTO dto = new TicketDTO();
        dto.setSubject("Admin ticket");
        dto.setMessage("Admin message");
        dto.setCategory("TECHNICAL");
        dto.setPriority("LOW");

        User user = new User();
        user.setId(userId);
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ticketService.createTicket(dto, username);

        // Then
        verify(ticketRepository).save(argThat(ticket ->
                ticket.getUser().equals(user) && ticket.getUser().getId().equals(userId)
        ));
    }

    // ==================== TESTY getUserTickets ====================

    @Test
    @DisplayName("getUserTickets - powinien zwrócić listę ticketów użytkownika")
    void getUserTickets_shouldReturnUserTickets() {
        // Given
        String username = "john.doe";
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setUsername(username);

        List<Ticket> tickets = Arrays.asList(
                createTicket("Ticket 1"),
                createTicket("Ticket 2"),
                createTicket("Ticket 3")
        );

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(ticketRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(tickets);

        // When
        List<Ticket> result = ticketService.getUserTickets(username);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactlyElementsOf(tickets);
        verify(userRepository).findByUsername(username);
        verify(ticketRepository).findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    @DisplayName("getUserTickets - powinien rzucić wyjątek gdy użytkownik nie istnieje")
    void getUserTickets_shouldThrowException_whenUserNotFound() {
        // Given
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ticketService.getUserTickets(username))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");

        verify(ticketRepository, never()).findByUserIdOrderByCreatedAtDesc(any());
    }

    @Test
    @DisplayName("getUserTickets - powinien zwrócić pustą listę gdy użytkownik nie ma ticketów")
    void getUserTickets_shouldReturnEmptyList_whenUserHasNoTickets() {
        // Given
        String username = "newuser";
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(ticketRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(Collections.emptyList());

        // When
        List<Ticket> result = ticketService.getUserTickets(username);

        // Then
        assertThat(result).isEmpty();
        verify(ticketRepository).findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    @DisplayName("getUserTickets - powinien wywołać repository z poprawnym userId")
    void getUserTickets_shouldCallRepository_withCorrectUserId() {
        // Given
        String username = "user123";
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(ticketRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(Collections.emptyList());

        // When
        ticketService.getUserTickets(username);

        // Then
        verify(ticketRepository).findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    @DisplayName("getUserTickets - powinien zachować kolejność ticketów z repository")
    void getUserTickets_shouldPreserveOrder_fromRepository() {
        // Given
        String username = "user";
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        Ticket ticket1 = createTicket("First");
        Ticket ticket2 = createTicket("Second");
        List<Ticket> orderedTickets = Arrays.asList(ticket1, ticket2);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(ticketRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(orderedTickets);

        // When
        List<Ticket> result = ticketService.getUserTickets(username);

        // Then
        assertThat(result).containsExactly(ticket1, ticket2);
    }

    // ==================== TESTY getAllTickets ====================

    @Test
    @DisplayName("getAllTickets - powinien zwrócić wszystkie tickety")
    void getAllTickets_shouldReturnAllTickets() {
        // Given
        List<Ticket> allTickets = Arrays.asList(
                createTicket("Ticket A"),
                createTicket("Ticket B"),
                createTicket("Ticket C")
        );

        when(ticketRepository.findAllByOrderByCreatedAtDesc()).thenReturn(allTickets);

        // When
        List<Ticket> result = ticketService.getAllTickets();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactlyElementsOf(allTickets);
        verify(ticketRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("getAllTickets - powinien zwrócić pustą listę gdy nie ma ticketów")
    void getAllTickets_shouldReturnEmptyList_whenNoTickets() {
        // Given
        when(ticketRepository.findAllByOrderByCreatedAtDesc()).thenReturn(Collections.emptyList());

        // When
        List<Ticket> result = ticketService.getAllTickets();

        // Then
        assertThat(result).isEmpty();
        verify(ticketRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("getAllTickets - powinien wywołać findAllByOrderByCreatedAtDesc")
    void getAllTickets_shouldCallFindAllByOrderByCreatedAtDesc() {
        // Given
        when(ticketRepository.findAllByOrderByCreatedAtDesc()).thenReturn(Collections.emptyList());

        // When
        ticketService.getAllTickets();

        // Then
        verify(ticketRepository, times(1)).findAllByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("getAllTickets - powinien zachować kolejność z repository")
    void getAllTickets_shouldPreserveOrder_fromRepository() {
        // Given
        Ticket newest = createTicket("Newest");
        Ticket older = createTicket("Older");
        Ticket oldest = createTicket("Oldest");
        List<Ticket> orderedTickets = Arrays.asList(newest, older, oldest);

        when(ticketRepository.findAllByOrderByCreatedAtDesc()).thenReturn(orderedTickets);

        // When
        List<Ticket> result = ticketService.getAllTickets();

        // Then
        assertThat(result).containsExactly(newest, older, oldest);
    }

    @Test
    @DisplayName("getAllTickets - nie powinien modyfikować listy z repository")
    void getAllTickets_shouldNotModifyList_fromRepository() {
        // Given
        List<Ticket> tickets = new ArrayList<>(Arrays.asList(createTicket("Test")));
        when(ticketRepository.findAllByOrderByCreatedAtDesc()).thenReturn(tickets);

        // When
        List<Ticket> result = ticketService.getAllTickets();

        // Then
        assertThat(result).isSameAs(tickets);
    }

    // ==================== TESTY getTicketById ====================

    @Test
    @DisplayName("getTicketById - powinien zwrócić ticket gdy istnieje")
    void getTicketById_shouldReturnTicket_whenExists() {
        // Given
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = createTicket("Test Ticket");
        ticket.setId(ticketId);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        // When
        Optional<Ticket> result = ticketService.getTicketById(ticketId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(ticket);
        verify(ticketRepository).findById(ticketId);
    }

    @Test
    @DisplayName("getTicketById - powinien zwrócić empty gdy ticket nie istnieje")
    void getTicketById_shouldReturnEmpty_whenNotExists() {
        // Given
        UUID ticketId = UUID.randomUUID();
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        // When
        Optional<Ticket> result = ticketService.getTicketById(ticketId);

        // Then
        assertThat(result).isEmpty();
        verify(ticketRepository).findById(ticketId);
    }

    @Test
    @DisplayName("getTicketById - powinien wywołać repository z poprawnym ID")
    void getTicketById_shouldCallRepository_withCorrectId() {
        // Given
        UUID ticketId = UUID.randomUUID();
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        // When
        ticketService.getTicketById(ticketId);

        // Then
        verify(ticketRepository).findById(ticketId);
    }

    @Test
    @DisplayName("getTicketById - powinien zwrócić ticket z wszystkimi danymi")
    void getTicketById_shouldReturnTicket_withAllData() {
        // Given
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = createTicket("Complete Ticket");
        ticket.setId(ticketId);
        ticket.setStatus(Ticket.Status.OPEN);
        ticket.setPriority(Ticket.Priority.HIGH);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        // When
        Optional<Ticket> result = ticketService.getTicketById(ticketId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(ticketId);
        assertThat(result.get().getStatus()).isEqualTo(Ticket.Status.OPEN);
        assertThat(result.get().getPriority()).isEqualTo(Ticket.Priority.HIGH);
    }

    @Test
    @DisplayName("getTicketById - powinien wywołać findById tylko raz")
    void getTicketById_shouldCallFindById_onlyOnce() {
        // Given
        UUID ticketId = UUID.randomUUID();
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        // When
        ticketService.getTicketById(ticketId);

        // Then
        verify(ticketRepository, times(1)).findById(ticketId);
    }

    // ==================== TESTY updateStatus ====================

    @Test
    @DisplayName("updateStatus - powinien zaktualizować status ticketu")
    void updateStatus_shouldUpdateStatus_successfully() {
        // Given
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = createTicket("Test");
        ticket.setStatus(Ticket.Status.OPEN);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Ticket result = ticketService.updateStatus(ticketId, "IN_PROGRESS");

        // Then
        assertThat(result.getStatus()).isEqualTo(Ticket.Status.IN_PROGRESS);
        verify(ticketRepository).findById(ticketId);
        verify(ticketRepository).save(ticket);
    }

    @Test
    @DisplayName("updateStatus - powinien rzucić wyjątek gdy ticket nie istnieje")
    void updateStatus_shouldThrowException_whenTicketNotFound() {
        // Given
        UUID ticketId = UUID.randomUUID();
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ticketService.updateStatus(ticketId, "RESOLVED"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ticket not found");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStatus - powinien rzucić wyjątek przy nieprawidłowym statusie")
    void updateStatus_shouldThrowException_whenInvalidStatus() {
        // Given
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = createTicket("Test");

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        // When & Then
        assertThatThrownBy(() -> ticketService.updateStatus(ticketId, "INVALID_STATUS"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid status");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStatus - powinien obsłużyć status w lowercase")
    void updateStatus_shouldHandleStatus_inLowercase() {
        // Given
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = createTicket("Test");

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Ticket result = ticketService.updateStatus(ticketId, "resolved");

        // Then
        assertThat(result.getStatus()).isEqualTo(Ticket.Status.RESOLVED);
    }

    @Test
    @DisplayName("updateStatus - powinien zapisać ticket po zmianie statusu")
    void updateStatus_shouldSaveTicket_afterStatusChange() {
        // Given
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = createTicket("Test");

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        // When
        ticketService.updateStatus(ticketId, "CLOSED");

        // Then
        verify(ticketRepository).save(ticket);
    }

    // ==================== TESTY updatePriority ====================

    @Test
    @DisplayName("updatePriority - powinien zaktualizować priorytet ticketu")
    void updatePriority_shouldUpdatePriority_successfully() {
        // Given
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = createTicket("Test");
        ticket.setPriority(Ticket.Priority.LOW);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Ticket result = ticketService.updatePriority(ticketId, "URGENT");

        // Then
        assertThat(result.getPriority()).isEqualTo(Ticket.Priority.URGENT);
        verify(ticketRepository).findById(ticketId);
        verify(ticketRepository).save(ticket);
    }

    @Test
    @DisplayName("updatePriority - powinien rzucić wyjątek gdy ticket nie istnieje")
    void updatePriority_shouldThrowException_whenTicketNotFound() {
        // Given
        UUID ticketId = UUID.randomUUID();
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ticketService.updatePriority(ticketId, "HIGH"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ticket not found");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("updatePriority - powinien rzucić wyjątek przy nieprawidłowym priorytecie")
    void updatePriority_shouldThrowException_whenInvalidPriority() {
        // Given
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = createTicket("Test");

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        // When & Then
        assertThatThrownBy(() -> ticketService.updatePriority(ticketId, "SUPER_HIGH"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid priority");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("updatePriority - powinien obsłużyć priorytet w lowercase")
    void updatePriority_shouldHandlePriority_inLowercase() {
        // Given
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = createTicket("Test");

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Ticket result = ticketService.updatePriority(ticketId, "high");

        // Then
        assertThat(result.getPriority()).isEqualTo(Ticket.Priority.HIGH);
    }

    @Test
    @DisplayName("updatePriority - powinien zapisać ticket po zmianie priorytetu")
    void updatePriority_shouldSaveTicket_afterPriorityChange() {
        // Given
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = createTicket("Test");

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        // When
        ticketService.updatePriority(ticketId, "MEDIUM");

        // Then
        verify(ticketRepository).save(ticket);
    }

    // ==================== TESTY addResponse ====================

    @Test
    @DisplayName("addResponse - powinien dodać odpowiedź do ticketu")
    void addResponse_shouldAddResponse_successfully() {
        // Given
        UUID ticketId = UUID.randomUUID();
        String message = "Odpowiedź na ticket";
        String username = "admin";

        Ticket ticket = createTicket("Test");
        User author = new User();
        author.setUsername(username);
        author.setRole(User.Role.USER);

        TicketResponse response = new TicketResponse();
        response.setMessage(message);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(author));
        when(responseRepository.save(any(TicketResponse.class))).thenReturn(response);

        // When
        TicketResponse result = ticketService.addResponse(ticketId, message, username);

        // Then
        assertThat(result).isNotNull();
        verify(ticketRepository).findById(ticketId);
        verify(userRepository).findByUsername(username);
        verify(responseRepository).save(any(TicketResponse.class));
    }

    @Test
    @DisplayName("addResponse - admin powinien zmienić status z OPEN na IN_PROGRESS")
    void addResponse_shouldChangeStatus_whenAdminRespondsToOpenTicket() {
        // Given
        UUID ticketId = UUID.randomUUID();
        String message = "Admin response";
        String username = "admin";

        Ticket ticket = createTicket("Test");
        ticket.setStatus(Ticket.Status.OPEN);

        User admin = new User();
        admin.setUsername(username);
        admin.setRole(User.Role.ADMIN);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(admin));
        when(responseRepository.save(any(TicketResponse.class))).thenReturn(new TicketResponse());
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        // When
        ticketService.addResponse(ticketId, message, username);

        // Then
        verify(ticketRepository).save(ticket);
        assertThat(ticket.getStatus()).isEqualTo(Ticket.Status.IN_PROGRESS);
    }

    @Test
    @DisplayName("addResponse - admin nie powinien zmieniać statusu jeśli ticket nie jest OPEN")
    void addResponse_shouldNotChangeStatus_whenTicketNotOpen() {
        // Given
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = createTicket("Test");
        ticket.setStatus(Ticket.Status.RESOLVED);

        User admin = new User();
        admin.setRole(User.Role.ADMIN);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(admin));
        when(responseRepository.save(any(TicketResponse.class))).thenReturn(new TicketResponse());

        // When
        ticketService.addResponse(ticketId, "message", "admin");

        // Then
        verify(ticketRepository, never()).save(ticket);
        assertThat(ticket.getStatus()).isEqualTo(Ticket.Status.RESOLVED);
    }

    @Test
    @DisplayName("addResponse - zwykły user nie powinien zmieniać statusu ticketu")
    void addResponse_shouldNotChangeStatus_whenRegularUserResponds() {
        // Given
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = createTicket("Test");
        ticket.setStatus(Ticket.Status.OPEN);

        User user = new User();
        user.setRole(User.Role.USER);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(responseRepository.save(any(TicketResponse.class))).thenReturn(new TicketResponse());

        // When
        ticketService.addResponse(ticketId, "message", "user");

        // Then
        verify(ticketRepository, never()).save(ticket);
        assertThat(ticket.getStatus()).isEqualTo(Ticket.Status.OPEN);
    }

    @Test
    @DisplayName("addResponse - powinien rzucić wyjątek gdy ticket nie istnieje")
    void addResponse_shouldThrowException_whenTicketNotFound() {
        // Given
        UUID ticketId = UUID.randomUUID();
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ticketService.addResponse(ticketId, "message", "user"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ticket not found");

        verify(responseRepository, never()).save(any());
    }

    // ==================== TESTY deleteTicket ====================

    @Test
    @DisplayName("deleteTicket - powinien usunąć ticket gdy istnieje")
    void deleteTicket_shouldDeleteTicket_whenExists() {
        // Given
        UUID ticketId = UUID.randomUUID();
        when(ticketRepository.existsById(ticketId)).thenReturn(true);
        doNothing().when(ticketRepository).deleteById(ticketId);

        // When
        ticketService.deleteTicket(ticketId);

        // Then
        verify(ticketRepository).existsById(ticketId);
        verify(ticketRepository).deleteById(ticketId);
    }

    @Test
    @DisplayName("deleteTicket - powinien rzucić wyjątek gdy ticket nie istnieje")
    void deleteTicket_shouldThrowException_whenTicketNotExists() {
        // Given
        UUID ticketId = UUID.randomUUID();
        when(ticketRepository.existsById(ticketId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> ticketService.deleteTicket(ticketId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ticket not found");

        verify(ticketRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("deleteTicket - powinien wywołać deleteById z poprawnym ID")
    void deleteTicket_shouldCallDeleteById_withCorrectId() {
        // Given
        UUID ticketId = UUID.randomUUID();
        when(ticketRepository.existsById(ticketId)).thenReturn(true);

        // When
        ticketService.deleteTicket(ticketId);

        // Then
        verify(ticketRepository).deleteById(ticketId);
    }

    @Test
    @DisplayName("deleteTicket - powinien najpierw sprawdzić istnienie ticketu")
    void deleteTicket_shouldCheckExistence_beforeDeleting() {
        // Given
        UUID ticketId = UUID.randomUUID();
        when(ticketRepository.existsById(ticketId)).thenReturn(true);

        // When
        ticketService.deleteTicket(ticketId);

        // Then
        var inOrder = inOrder(ticketRepository);
        inOrder.verify(ticketRepository).existsById(ticketId);
        inOrder.verify(ticketRepository).deleteById(ticketId);
    }

    @Test
    @DisplayName("deleteTicket - powinien wywołać existsById tylko raz")
    void deleteTicket_shouldCallExistsById_onlyOnce() {
        // Given
        UUID ticketId = UUID.randomUUID();
        when(ticketRepository.existsById(ticketId)).thenReturn(true);

        // When
        ticketService.deleteTicket(ticketId);

        // Then
        verify(ticketRepository, times(1)).existsById(ticketId);
    }

    // ==================== TESTY getStats ====================

    @Test
    @DisplayName("getStats - powinien zwrócić statystyki ticketów")
    void getStats_shouldReturnStatistics() {
        // Given
        when(ticketRepository.count()).thenReturn(100L);
        when(ticketRepository.countByStatus(Ticket.Status.OPEN)).thenReturn(20L);
        when(ticketRepository.countByStatus(Ticket.Status.IN_PROGRESS)).thenReturn(15L);
        when(ticketRepository.countByStatus(Ticket.Status.WAITING)).thenReturn(10L);
        when(ticketRepository.countByStatus(Ticket.Status.RESOLVED)).thenReturn(30L);
        when(ticketRepository.countByStatus(Ticket.Status.CLOSED)).thenReturn(25L);
        when(ticketRepository.countTodayTickets(any(LocalDateTime.class))).thenReturn(5L);

        // When
        Map<String, Object> stats = ticketService.getStats();

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.get("total")).isEqualTo(100L);
        assertThat(stats.get("open")).isEqualTo(20L);
        assertThat(stats.get("inProgress")).isEqualTo(15L);
        assertThat(stats.get("waiting")).isEqualTo(10L);
        assertThat(stats.get("resolved")).isEqualTo(30L);
        assertThat(stats.get("closed")).isEqualTo(25L);
        assertThat(stats.get("todayNew")).isEqualTo(5L);
    }

    @Test
    @DisplayName("getStats - powinien zawierać wszystkie wymagane klucze")
    void getStats_shouldContainAllRequiredKeys() {
        // Given
        when(ticketRepository.count()).thenReturn(0L);
        when(ticketRepository.countByStatus(any())).thenReturn(0L);
        when(ticketRepository.countTodayTickets(any())).thenReturn(0L);

        // When
        Map<String, Object> stats = ticketService.getStats();

        // Then
        assertThat(stats).containsKeys(
                "total", "open", "inProgress", "waiting",
                "resolved", "closed", "todayNew", "avgResponseTime"
        );
    }

    @Test
    @DisplayName("getStats - powinien wywołać count dla każdego statusu")
    void getStats_shouldCallCount_forEachStatus() {
        // Given
        when(ticketRepository.count()).thenReturn(0L);
        when(ticketRepository.countByStatus(any())).thenReturn(0L);
        when(ticketRepository.countTodayTickets(any())).thenReturn(0L);

        // When
        ticketService.getStats();

        // Then
        verify(ticketRepository).countByStatus(Ticket.Status.OPEN);
        verify(ticketRepository).countByStatus(Ticket.Status.IN_PROGRESS);
        verify(ticketRepository).countByStatus(Ticket.Status.WAITING);
        verify(ticketRepository).countByStatus(Ticket.Status.RESOLVED);
        verify(ticketRepository).countByStatus(Ticket.Status.CLOSED);
    }

    @Test
    @DisplayName("getStats - powinien wywołać countTodayTickets z dzisiejszą datą")
    void getStats_shouldCallCountTodayTickets_withTodayDate() {
        // Given
        when(ticketRepository.count()).thenReturn(0L);
        when(ticketRepository.countByStatus(any())).thenReturn(0L);
        when(ticketRepository.countTodayTickets(any())).thenReturn(0L);

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();

        // When
        ticketService.getStats();

        // Then
        verify(ticketRepository).countTodayTickets(argThat(date ->
                date.toLocalDate().equals(today) &&
                        date.toLocalTime().equals(startOfDay.toLocalTime())
        ));
    }

    @Test
    @DisplayName("getStats - powinien zwrócić poprawne wartości gdy brak ticketów")
    void getStats_shouldReturnZeros_whenNoTickets() {
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
        return ticket;
    }
}