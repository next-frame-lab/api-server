package wisoft.nextframe.schedulereservationticketing.reservation.domain;

import java.util.Objects;
import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class ReservationId {
	private final UUID value;

	private ReservationId(UUID value) {
		this.value = Objects.requireNonNull(value, "ReservationId는 null일 수 없습니다.");
	}

	public static ReservationId of(UUID value) {
		return new ReservationId(value);
	}

	public static ReservationId of() {
		return new ReservationId(UUID.randomUUID());
	}
}
