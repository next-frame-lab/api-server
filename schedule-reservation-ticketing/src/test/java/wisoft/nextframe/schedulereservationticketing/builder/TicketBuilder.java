package wisoft.nextframe.schedulereservationticketing.builder;

import java.time.LocalDateTime;
import java.util.UUID;

import wisoft.nextframe.schedulereservationticketing.entity.reservation.Reservation;
import wisoft.nextframe.schedulereservationticketing.entity.ticketing.Ticket;

public class TicketBuilder {

	private UUID id = UUID.randomUUID();
	private Reservation reservation;
	private LocalDateTime issuedAt = LocalDateTime.now();
	private String qrCode = "QR-" + UUID.randomUUID();
	private boolean isUsed = false;

	public TicketBuilder withId(UUID id) {
		this.id = id;
		return this;
	}

	public TicketBuilder withReservation(Reservation reservation) {
		this.reservation = reservation;
		return this;
	}

	public TicketBuilder withIssuedAt(LocalDateTime issuedAt) {
		this.issuedAt = issuedAt;
		return this;
	}

	public TicketBuilder withQrCode(String qrCode) {
		this.qrCode = qrCode;
		return this;
	}

	public TicketBuilder withIsUsed(boolean isUsed) {
		this.isUsed = isUsed;
		return this;
	}

	public Ticket build() {
		// Ticket 생성자 방식에 따라 조정 필요
		return new Ticket(id, reservation, issuedAt, isUsed, qrCode);
	}
}
