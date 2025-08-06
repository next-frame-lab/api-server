package wisoft.nextframe.util;

import java.util.Set;

import wisoft.nextframe.common.Money;
import wisoft.nextframe.schedule.PerformanceFixture;
import wisoft.nextframe.schedule.domain.performance.Performance;
import wisoft.nextframe.reservation.domain.Reservation;
import wisoft.nextframe.schedule.domain.seat.Seat;
import wisoft.nextframe.user.domain.User;

public class ReservationFixture {

	private static final Money DEFAULT_TOTAL_PRICE = Money.of(300_000);

	private ReservationFixture() {
	}

	public static Reservation defaultReservation() {
		return create(
			UserFixture.defaultUser(),
			PerformanceFixture.defaultPerformance(),
			Set.of(SeatFixture.available("A", 2, 1))
		);
	}

	public static Reservation withSeats(Set<Seat> seats) {
		return create(
			UserFixture.defaultUser(),
			PerformanceFixture.defaultPerformance(),
			seats
		);
	}

	private static Reservation create(User user, Performance performance, Set<Seat> seats) {
		return Reservation.create(user, performance, seats, DEFAULT_TOTAL_PRICE);
	}
}

