package wisoft.nextframe.domain.performance;

import java.util.Objects;
import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class PerformanceId {

	private final UUID value;

	private PerformanceId(UUID value) {
		this.value = Objects.requireNonNull(value, "PerformanceId는 null일 수 없습니다.");
	}

	public static PerformanceId of(UUID value) {
		return new PerformanceId(value);
	}

	public static PerformanceId generate() {
		return new PerformanceId(UUID.randomUUID());
	}

}
