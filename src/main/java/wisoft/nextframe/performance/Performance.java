package wisoft.nextframe.performance;

import java.time.LocalDateTime;

import lombok.Getter;
import wisoft.nextframe.stadium.Stadium;
import wisoft.nextframe.user.User;

public class Performance {

	private final PerformanceId id;
	private final PerformanceProfile profile; // 공연 기본 정보
	private final Schedule schedule;
	@Getter
	private final int basePrice;
	@Getter
	private final Stadium stadium;
	private final ReservablePeriod reservablePeriod;

	private Performance(
		PerformanceId id, PerformanceProfile profile,
		Schedule schedule,
		int basePrice,
		Stadium stadium,
		ReservablePeriod reservablePeriod
	) {
		this.id = id;
		this.profile = profile;
		this.schedule = schedule;
		this.basePrice = basePrice;
		this.stadium = stadium;
		this.reservablePeriod = reservablePeriod;
	}

	public static Performance create(
		PerformanceProfile profile,
		Schedule schedule,
		int basePrice,
		Stadium stadium,
		ReservablePeriod reservablePeriod
	) {
		return new Performance(PerformanceId.generate(), profile, schedule, basePrice, stadium, reservablePeriod);
	}

	public boolean isReservableBy(User user) {
		return profile.isReservableBy(user);
	}

	public boolean isStarted() {
		return schedule.hasStarted(LocalDateTime.now());
	}

	public boolean isReservableNow() {
		return reservablePeriod.isOpen(LocalDateTime.now());
	}
}
