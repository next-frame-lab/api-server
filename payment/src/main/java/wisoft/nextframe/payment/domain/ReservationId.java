package wisoft.nextframe.payment.domain;

import java.util.Objects;
import java.util.UUID;

public record ReservationId(UUID value) {

	public ReservationId {
		Objects.requireNonNull(value, "ReservationId는 null일 수 없습니다.");
	}

	public static ReservationId of(UUID value) {
		return new ReservationId(value);
	}

	public static ReservationId of() {
		return new ReservationId(UUID.randomUUID());
	}
}
