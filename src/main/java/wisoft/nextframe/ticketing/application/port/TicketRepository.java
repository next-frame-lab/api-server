package wisoft.nextframe.ticketing.application.port;

import java.util.UUID;

import wisoft.nextframe.ticketing.domain.Ticket;

public interface TicketRepository {
	Ticket save(Ticket ticket, UUID reservationId, UUID seatId, UUID scheduleId);
}
