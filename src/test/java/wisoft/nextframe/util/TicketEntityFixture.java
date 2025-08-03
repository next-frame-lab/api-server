package wisoft.nextframe.util;

import java.time.LocalDateTime;
import java.util.UUID;

import wisoft.nextframe.domain.reservation.ReservationId;
import wisoft.nextframe.domain.ticket.Ticket;
import wisoft.nextframe.domain.ticket.TicketId;
import wisoft.nextframe.infra.ticket.TicketEntity;

public class TicketEntityFixture {

	public static final UUID DEFAULT_TICKET_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	public static final UUID DEFAULT_RESERVATION_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	public static final UUID DEFAULT_SEAT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
	public static final UUID DEFAULT_SCHEDULE_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
	public static final String DEFAULT_QR = "QR-1111-2222";
	public static final LocalDateTime DEFAULT_ISSUED_AT = LocalDateTime.of(2025, 7, 31, 12, 0);

	public static TicketEntity sampleEntity() {
		return TicketEntity.builder()
			.id(DEFAULT_TICKET_ID)
			.reservationId(DEFAULT_RESERVATION_ID)
			.seatId(DEFAULT_SEAT_ID)
			.scheduleId(DEFAULT_SCHEDULE_ID)
			.qrCode(DEFAULT_QR)
			.issuedAt(DEFAULT_ISSUED_AT)
			.isUsed(false)
			.build();
	}

	public static Ticket sampleDomain() {
		return Ticket.reconstruct(
			TicketId.of(DEFAULT_TICKET_ID),
			ReservationId.of(DEFAULT_RESERVATION_ID),
			DEFAULT_QR,
			DEFAULT_ISSUED_AT
		);
	}
}
