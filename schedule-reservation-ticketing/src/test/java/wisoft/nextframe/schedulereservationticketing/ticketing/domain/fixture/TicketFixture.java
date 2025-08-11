package wisoft.nextframe.schedulereservationticketing.ticketing.domain.fixture;

import java.time.LocalDateTime;
import java.util.UUID;

import wisoft.nextframe.reservation.domain.ReservationId;
import wisoft.nextframe.ticketing.domain.Ticket;
import wisoft.nextframe.ticketing.domain.TicketId;

public class TicketFixture {

	public static final UUID DEFAULT_TICKET_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	public static final UUID DEFAULT_RESERVATION_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	public static final String DEFAULT_QR = "QR-1111-2222";
	public static final LocalDateTime DEFAULT_ISSUED_AT = LocalDateTime.of(2025, 7, 31, 12, 0);

	public static Ticket sampleTicket() {
		return Ticket.reconstruct(
			TicketId.of(DEFAULT_TICKET_ID),
			ReservationId.of(DEFAULT_RESERVATION_ID),
			DEFAULT_QR,
			DEFAULT_ISSUED_AT
		);
	}
}
