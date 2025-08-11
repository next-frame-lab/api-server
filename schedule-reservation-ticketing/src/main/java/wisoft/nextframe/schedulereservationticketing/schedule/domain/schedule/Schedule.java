package wisoft.nextframe.schedulereservationticketing.schedule.domain.schedule;

import java.time.LocalDateTime;

public class Schedule {

	private final LocalDateTime startTime;

	public Schedule(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public boolean hasStarted(LocalDateTime now) {
		return now.isAfter(startTime);
	}

}
