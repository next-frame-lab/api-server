package wisoft.nextframe.schedulereservationticketing.schedule.domain.performance;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import wisoft.nextframe.schedulereservationticketing.common.Money;
import wisoft.nextframe.schedulereservationticketing.schedule.domain.schedule.ReservablePeriod;
import wisoft.nextframe.schedulereservationticketing.schedule.domain.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.schedule.domain.schedule.exception.SeatSectionNotDefinedException;
import wisoft.nextframe.schedulereservationticketing.schedule.domain.seat.Seat;
import wisoft.nextframe.schedulereservationticketing.schedule.domain.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.user.domain.User;

@Getter
public class Performance {

	private final PerformanceId id;
	private final PerformanceProfile profile;
	private final Schedule schedule;
	private final Map<String, Money> sectionPrice;
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

	/**
	 * [도메인 객체 복원용 팩토리 메서드]
	 *
	 * 이 메서드는 DB 조회, JPA 매핑, 외부 시스템(JSON, Kafka 등)으로부터 전달받은 데이터를 바탕으로
	 * 도메인 객체를 복원할 때만 사용해야 합니다.
	 *
	 * 도메인 내부의 비즈니스 로직에서는 절대 이 메서드를 직접 사용하지 마세요.
	 * 새로운 Performance를 생성하려면 {@link #create(PerformanceProfile, Schedule, Money, Stadium, ReservablePeriod)}를 사용하세요.
	 *
	 */
	public static Performance reconstruct(
		PerformanceId id,
		PerformanceProfile profile,
		Schedule schedule,
		Map<String, Money> sectionPrice,
		Stadium stadium,
		ReservablePeriod reservablePeriod
	) {
		return new Performance(id, profile, schedule, sectionPrice, stadium, reservablePeriod);
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
