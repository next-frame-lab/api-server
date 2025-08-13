package wisoft.nextframe.schedulereservationticketing.entity.seat;

import java.io.Serializable;
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
@Builder
@AllArgsConstructor
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class SeatStateId implements Serializable {

	@Column(name = "schedule_id", nullable = false)
	private UUID scheduleId;

	@Column(name = "seat_id", nullable = false)
	private UUID seatId;
}