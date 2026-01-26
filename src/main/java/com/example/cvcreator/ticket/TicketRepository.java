package com.example.cvcreator.ticket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    List<Ticket> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Ticket> findAllByOrderByCreatedAtDesc();

    long countByStatus(Ticket.Status status);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.createdAt >= :startOfDay")
    long countTodayTickets(@Param("startOfDay") LocalDateTime startOfDay);
}

