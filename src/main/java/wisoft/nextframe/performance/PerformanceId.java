package wisoft.nextframe.performance;

import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class PerformanceId {

	private final UUID value;

	private PerformanceId(UUID value) {
		this.value = value;
	}

	public static PerformanceId of(UUID value) {
		return new PerformanceId(value);
	}

	public static PerformanceId generate() {
		return new PerformanceId(UUID.randomUUID());
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
