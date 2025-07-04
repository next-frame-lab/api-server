package wisoft.nextframe.reservation;

import java.util.UUID;

import lombok.Getter;

@Getter
public class ReservationId {
	private final UUID value;

	private ReservationId(UUID value) {
		this.value = value;
	}

	public static ReservationId of(UUID value) {
		if (value == null) {
			throw new IllegalArgumentException("ReservationId cannot be null");
		}
		return new ReservationId(value);
	}

	@Override
	public String toString() {
		return value.toString();
	}
}