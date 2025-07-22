package wisoft.nextframe.domain.performance;

import java.time.LocalDateTime;

public class ReservablePeriod {

	private final LocalDateTime openTime; 	// 예매 시작 시간
	private final LocalDateTime closeTime; 	// 예매 종료 시간

	public ReservablePeriod(LocalDateTime openTime, LocalDateTime closeTime) {
		this.openTime = openTime;
		this.closeTime = closeTime;
	}

	public boolean isOpen(LocalDateTime now) {
		return !now.isBefore(openTime) && !now.isAfter(closeTime);
	}
}
