package wisoft.nextframe.infra.ticket;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "tickets")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TicketEntity {

	@Id
	@Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
	private UUID id;

	@Column(name = "reservation_id", nullable = false, columnDefinition = "uuid")
	private UUID reservationId;

	@Column(name = "seat_id", nullable = false, columnDefinition = "uuid")
	private UUID seatId;

	@Column(name = "schedule_id", nullable = false, columnDefinition = "uuid")
	private UUID scheduleId;

	@Column(name = "issued_at", nullable = false)
	private LocalDateTime issuedAt;

	@Column(name = "is_used", nullable = false)
	private boolean isUsed;

	@Column(name = "qr_code", nullable = false)
	private String qrCode;

}
