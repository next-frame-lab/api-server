package wisoft.nextframe.ticket;

import java.time.LocalDateTime;
import java.util.UUID;

import wisoft.nextframe.payment.Payment;

public class Ticket {

	private final String qrCode;
	private final LocalDateTime issuedAt;

	public Ticket(String qrCode, LocalDateTime issuedAt) {
		this.qrCode = qrCode;
		this.issuedAt = issuedAt;
	}

	public static Ticket createFrom(Payment payment) {
		UUID reservationId = payment.getReservationId();
		UUID ticketId = UUID.randomUUID();

		String qr = "QR-" + reservationId + "-" + ticketId;

		return new Ticket(qr, LocalDateTime.now());
	}

	public String getQrCode() {
		return qrCode;
	}

	public LocalDateTime getIssuedAt() {
		return issuedAt;
	}
}
