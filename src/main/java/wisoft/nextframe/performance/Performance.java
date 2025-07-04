package wisoft.nextframe.performance;

import java.time.LocalDateTime;

import lombok.Getter;
import wisoft.nextframe.stadium.Stadium;
import wisoft.nextframe.user.User;

@Getter
public class Performance {

	private final PerformanceInfo info;
	private final PerformanceDetail detail;
	private final Schedule schedule;
	private final int basePrice;
	private final Stadium stadium;
	private final ReservationWindow reservationWindow;

	private Performance(
		PerformanceInfo info,
		PerformanceDetail detail,
		Schedule schedule,
		int basePrice,
		Stadium stadium,
		ReservationWindow reservationWindow
	) {
		this.info = info;
		this.detail = detail;
		this.schedule = schedule;
		this.basePrice = basePrice;
		this.stadium = stadium;
		this.reservationWindow = reservationWindow;
	}

	public static Performance create(
		PerformanceInfo info,
		PerformanceDetail detail,
		Schedule schedule,
		int basePrice,
		Stadium stadium,
		ReservationWindow reservationWindow
	) {
		return new Performance(info, detail, schedule, basePrice, stadium, reservationWindow);
	}

	public boolean isReservableBy(User user) {
		return detail.isReservableBy(user);
	}

	public boolean isStarted() {
		return schedule.hasStarted(LocalDateTime.now().withNano(0));
	}

	public boolean isReservableNow() {
		return reservationWindow.isOpen(LocalDateTime.now().withNano(0));
	}

	public int getBasePrice() {
		return basePrice;
	}

	public Stadium getStadium() {
		return stadium;
	}
}
