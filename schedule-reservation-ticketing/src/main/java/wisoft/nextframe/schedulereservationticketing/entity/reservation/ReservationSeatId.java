package wisoft.nextframe.schedulereservationticketing.entity.reservation;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@Embeddable
public class ReservationSeatId {

	@Column(name = "reservation_id")
	private UUID reservationId;

	@Column(name = "seat_id")
	private UUID seatId;
}
