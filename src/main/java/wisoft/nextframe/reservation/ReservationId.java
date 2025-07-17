package wisoft.nextframe.reservation;

import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class ReservationId {
	private final UUID value;

	private ReservationId(UUID value) {
		this.value = value;
	}

	public static ReservationId of(UUID value) {
		return new ReservationId(value);
	}

	public static ReservationId generate() {
		return new ReservationId(UUID.randomUUID());
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
