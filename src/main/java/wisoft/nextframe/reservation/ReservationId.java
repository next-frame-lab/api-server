package wisoft.nextframe.reservation;

import java.util.Objects;
import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.ToString;

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

	public static ReservationId generate() {
		return new ReservationId(UUID.randomUUID());
	}
}
