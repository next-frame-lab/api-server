package wisoft.nextframe.reservation;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class ElapsedTime {

	private static final long MAX_SECONDS = 600L;
	private final long seconds;

	private ElapsedTime(long seconds) {
		if (seconds < 0) {
			throw new IllegalArgumentException("경과 시간은 0보다 커야 합니다.");
		}
		this.seconds = seconds;
	}

	public static ElapsedTime of(long seconds) {
		return new ElapsedTime(seconds);
	}

	public boolean isExceeded() {
		return seconds > MAX_SECONDS;
	}
}
