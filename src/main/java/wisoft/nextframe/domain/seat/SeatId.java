package wisoft.nextframe.domain.seat;

import java.util.Objects;
import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class SeatId {

	private final UUID value;

	private SeatId(UUID value) {
		this.value = Objects.requireNonNull(value, "SeatId는 null일 수 없습니다.");
	}

	public static SeatId of(UUID value) {
		return new SeatId(value);
	}

	public static SeatId of() {
		return new SeatId(UUID.randomUUID());
	}

}
