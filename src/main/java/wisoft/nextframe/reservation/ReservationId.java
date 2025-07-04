package wisoft.nextframe.reservation;

import java.util.Objects;
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
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ReservationId that = (ReservationId)obj;
		return Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(value);
	}

	@Override
	public String toString() {
		return value.toString();
	}
}