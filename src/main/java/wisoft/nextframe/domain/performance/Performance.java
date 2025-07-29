package wisoft.nextframe.domain.performance;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import wisoft.nextframe.common.Money;
import wisoft.nextframe.domain.performance.exception.SeatSectionNotDefinedException;
import wisoft.nextframe.domain.seat.Seat;
import wisoft.nextframe.domain.stadium.Stadium;
import wisoft.nextframe.domain.user.User;

public class Performance {

	private final PerformanceId id;
	private final PerformanceProfile profile;
	private final Schedule schedule;
	private final Map<String, Money> sectionPrice;
	@Getter
	private final Stadium stadium;
	private final ReservablePeriod reservablePeriod;

	private Performance(
		PerformanceId id,
		PerformanceProfile profile,
		Schedule schedule,
		Map<String, Money> sectionPrice,
		Stadium stadium,
		ReservablePeriod reservablePeriod
	) {
		this.id = id;
		this.profile = profile;
		this.schedule = schedule;
		this.sectionPrice = sectionPrice;
		this.stadium = stadium;
		this.reservablePeriod = reservablePeriod;
	}

	public static Performance create(
		PerformanceProfile profile,
		Schedule schedule,
		Map<String, Money> sectionPrice,
		Stadium stadium,
		ReservablePeriod reservablePeriod
	) {
		return new Performance(PerformanceId.generate(), profile, schedule, sectionPrice, stadium, reservablePeriod);
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

	private Money getPriceBySection(String section) {
		Money price = sectionPrice.get(section);
		if (price == null) {
			throw new SeatSectionNotDefinedException();
		}
		return price;
	}

	public Money calculateTotalPrice(Set<Seat> seats) {
		return seats.stream()
			.map(seat -> getPriceBySection(seat.getSection()))
			.reduce(Money.ZERO, Money::plus);
	}
}
