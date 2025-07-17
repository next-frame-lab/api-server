package wisoft.nextframe.seat;

import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class SeatId {

	private final UUID value;

	private SeatId(UUID value) {
		this.value = value;
	}

	public static SeatId of(UUID value) {
		return new SeatId(value);
	}

	public static SeatId generate() {
		return new SeatId(UUID.randomUUID());
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
