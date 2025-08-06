package wisoft.nextframe.ticketing.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.reservation.domain.ReservationId;

@Getter
public class Ticket {

	private final TicketId ticketId;
	private final ReservationId reservationId;
	private final String qrCode;
	private final LocalDateTime issuedAt;

	private Ticket(TicketId ticketId, ReservationId reservationId, String qrCode, LocalDateTime issuedAt) {
		this.ticketId = ticketId;
		this.reservationId = reservationId;
		this.qrCode = qrCode;
		this.issuedAt = issuedAt;
	}

	public static Ticket reconstruct(TicketId id, ReservationId reservationId, String qrCode, LocalDateTime issuedAt) {
		return new Ticket(id, reservationId, qrCode, issuedAt);
	}

	public static Ticket issueFrom(Payment payment) {
		ReservationId reservationId = payment.getReservationId();
		TicketId ticketId = TicketId.generate();

		String qr = "QR-" + reservationId + "-" + ticketId;

		return new Ticket(ticketId, reservationId, qr, LocalDateTime.now());
	}

}
