package wisoft.nextframe.schedulereservationticketing.repository.ticketing;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import wisoft.nextframe.schedulereservationticketing.dto.ticketing.TicketInfoResponse;
import wisoft.nextframe.schedulereservationticketing.entity.ticketing.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
	Optional<Ticket> findByReservationId(UUID reservationId);

	@Query("""
		    SELECT t.id, t.issuedAt, t.qrCode,
		           p.name, sd.rowNo, sd.columnNo
		    FROM Ticket t
		    JOIN t.reservation r
		    JOIN r.schedule sch
		    JOIN sch.performance p
		    JOIN r.reservationSeats rs
		    JOIN rs.seatDefinition sd
		    WHERE t.id = :ticketId
		""")
	Optional<TicketInfoResponse> findTicketInfoById(@Param("ticketId") UUID ticketId);
}