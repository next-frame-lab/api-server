package wisoft.nextframe.schedulereservationticketing.ticketing.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import wisoft.nextframe.schedulereservationticketing.ticketing.entity.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
	Optional<Ticket> findByReservationId(UUID reservationId);

}