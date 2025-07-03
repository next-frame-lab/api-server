package wisoft.nextframe.schedulereservationticketing.entity.ticketing;

import static jakarta.persistence.FetchType.*;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.Reservation;

@Entity
@Getter
@Table(name = "tickets")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Ticket {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
	private UUID id;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "reservation_id", nullable = false)
	private Reservation reservation;

	// // TODO: reservationId 기반으로 seatId, scheduleId 조회 후 세팅
	// @Column(name = "seat_id", nullable = true, columnDefinition = "uuid")
	// private UUID seatId;
	//
	// @Column(name = "schedule_id", nullable = true, columnDefinition = "uuid")
	// private UUID scheduleId;

	@Column(name = "issued_at", nullable = false)
	private LocalDateTime issuedAt;

	@Column(name = "is_used", nullable = false)
	private boolean isUsed;

	@Column(name = "qr_code", nullable = false)
	private String qrCode;

	// seatId, scheduleId는 추후 조회 후 세팅
	public static Ticket issue(Reservation reservation) {
		Ticket e = new Ticket();
		e.reservation = reservation;
		e.qrCode = "QR-" + reservation.getId() + "-" + UUID.randomUUID();
		e.issuedAt = LocalDateTime.now();
		e.isUsed = false;
		return e;
	}
}
