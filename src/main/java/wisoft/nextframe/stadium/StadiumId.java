package wisoft.nextframe.stadium;

import java.util.Objects;
import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class StadiumId {

	private final UUID value;

	private StadiumId(UUID value) {
		this.value = Objects.requireNonNull(value, "StadiumId는 null일 수 없습니다.");
	}

	public static StadiumId of(UUID value) {
		return new StadiumId(value);
	}

	public static StadiumId generate() {
		return new StadiumId(UUID.randomUUID());
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
