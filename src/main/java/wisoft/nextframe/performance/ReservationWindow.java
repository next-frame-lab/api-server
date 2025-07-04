package wisoft.nextframe.performance;

import java.time.LocalDateTime;

public class ReservationWindow {

	private final LocalDateTime start;
	private final LocalDateTime end;

	public ReservationWindow(LocalDateTime start, LocalDateTime end) {
		this.start = start;
		this.end = end;
	}

	public boolean isOpen(LocalDateTime now) {
		return !now.isBefore(start) && !now.isAfter(end);
	}
}
