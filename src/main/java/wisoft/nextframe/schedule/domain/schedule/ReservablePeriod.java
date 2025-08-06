package wisoft.nextframe.schedule.domain.schedule;

import java.time.LocalDateTime;

public class ReservablePeriod {

	private final LocalDateTime ticketOpenTime; 	// 예매 시작 시간
	private final LocalDateTime ticketCloseTime; 	// 예매 종료 시간

	public ReservablePeriod(LocalDateTime ticketOpenTime, LocalDateTime ticketCloseTime) {
		this.ticketOpenTime = ticketOpenTime;
		this.ticketCloseTime = ticketCloseTime;
	}

	public boolean isOpen(LocalDateTime now) {
		return !now.isBefore(ticketOpenTime) && !now.isAfter(ticketCloseTime);
	}
}
