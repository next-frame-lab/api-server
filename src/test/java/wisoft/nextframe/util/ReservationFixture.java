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
		return create(
			UserFixture.defaultUser(),
			PerformanceFixture.defaultPerformance(),
			Set.of(SeatFixture.available("일반", "B", 1)),
			DEFAULT_ELAPSED_TIME
		);
	}

	public static Reservation createWithUserAndPerformance(User user, Performance performance) {
		return create(
			user,
			performance,
			Set.of(SeatFixture.available("일반", "B", 1)),
			DEFAULT_ELAPSED_TIME
		);
	}

	public static Reservation createWithSeats(Set<Seat> seats) {
		return create(
			UserFixture.defaultUser(),
			PerformanceFixture.defaultPerformance(),
			seats,
			DEFAULT_ELAPSED_TIME
		);
	}

	public static Reservation createWithPerformance(Performance performance) {
		return create(
			UserFixture.defaultUser(),
			performance,
			Set.of(SeatFixture.available("일반", "B", 1)),
			DEFAULT_ELAPSED_TIME
		);
	}

	public static Reservation createWithElapsedTime(Long elapsedTime) {
		return create(
			UserFixture.defaultUser(),
			PerformanceFixture.defaultPerformance(),
			Set.of(SeatFixture.available("일반", "B", 1)),
			elapsedTime
		);
	}

	public static Reservation createWithPerformanceAndSeats(Performance performance, Set<Seat> seats) {
		return create(
			UserFixture.defaultUser(),
			performance,
			seats,
			DEFAULT_ELAPSED_TIME
		);
	}

	private static Reservation create(User user, Performance performance, Set<Seat> seats, Long elapsedTime) {
		return Reservation.create(
			user,
			performance,
			seats,
			elapsedTime
		);
	}
}
