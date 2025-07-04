package wisoft.nextframe.util;

import java.util.Set;

import wisoft.nextframe.performance.Performance;
import wisoft.nextframe.reservation.Reservation;
import wisoft.nextframe.seat.Seat;
import wisoft.nextframe.user.User;

public class ReservationFixture {

	private static final Long DEFAULT_ELAPSED_TIME = 300L;

	private ReservationFixture() {
	}

	public static Reservation defaultReservation() {
		return create(UserFixture.create(), PerformanceFixture.defaultPerformance(), Set.of(SeatFixture.available("일반", "B", 1)), DEFAULT_ELAPSED_TIME);
	}

	public static Reservation createWith(User user, Performance performance) {
		return create(user, performance, Set.of(SeatFixture.available("일반", "B", 1)), DEFAULT_ELAPSED_TIME);
	}

	public static Reservation createWith(Set<Seat> seats) {
		return create(UserFixture.create(), PerformanceFixture.defaultPerformance(), seats, DEFAULT_ELAPSED_TIME);
	}

	public static Reservation createWith(Performance performance) {
		return create(UserFixture.create(), performance, Set.of(SeatFixture.available("일반", "B", 1)), DEFAULT_ELAPSED_TIME);
	}

	public static Reservation createWith(Long elapsedTime) {
		return create(UserFixture.create(), PerformanceFixture.defaultPerformance(), Set.of(SeatFixture.available("일반", "B", 1)), elapsedTime);
	}

	public static Reservation createWith(Performance performance, Set<Seat> seats) {
		return create(UserFixture.create(), performance, seats, DEFAULT_ELAPSED_TIME);
	}

	private static Reservation create(User user, Performance performance, Set<Seat> seats, Long elapsedTime) {
		return Reservation.create(user, performance, seats, elapsedTime);
	}
}
