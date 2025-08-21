package wisoft.nextframe.schedulereservationticketing.entity.reservation;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@Embeddable
public class ReservationSeatId implements Serializable {

	@Setter
	@Column(name = "reservation_id")
	private UUID reservationId;

	@Column(name = "seat_id")
	private UUID seatId;

	public ReservationSeatId(UUID reservationId, UUID seatId) {
		this.reservationId = reservationId;
		this.seatId = seatId;
	}
}
