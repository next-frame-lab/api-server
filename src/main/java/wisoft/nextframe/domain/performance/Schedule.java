package wisoft.nextframe.domain.performance;

import java.time.Duration;
import java.time.LocalDateTime;

public class Schedule {

	private final LocalDateTime startTime;
	private final Duration runningTime;

	public Schedule(LocalDateTime startTime, Duration runningTime) {
		this.startTime = startTime;
		this.runningTime = runningTime;
	}

	public boolean hasStarted(LocalDateTime now) {
		return now.isAfter(startTime);
	}

	public LocalDateTime getEndTime() {
		return startTime.plus(runningTime);
	}
}
