package wisoft.nextframe.util;

import java.util.Set;

import wisoft.nextframe.common.Money;
import wisoft.nextframe.performance.Performance;
import wisoft.nextframe.reservation.Reservation;
import wisoft.nextframe.seat.Seat;
import wisoft.nextframe.user.User;

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

