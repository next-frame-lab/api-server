package wisoft.nextframe.reservation;

import java.util.Set;

import wisoft.nextframe.performance.Performance;
import wisoft.nextframe.performance.PerformancePolicy;
import wisoft.nextframe.reservation.exception.ReservationTimeLimitExceededException;
import wisoft.nextframe.seat.SeatPolicy;
import wisoft.nextframe.seat.Seat;
import wisoft.nextframe.user.User;

public class ReservationPolicy {

	public static void validateBeforeReservation(
		User user,
		Performance performance,
		Set<Seat> selectedSeats,
		Long elapsedTime
	) {
		PerformancePolicy.validateAdultOnly(user, performance);
		PerformancePolicy.validateNotStarted(performance);
		PerformancePolicy.validateReservationTime(performance);

		SeatPolicy.validateSeatsCount(selectedSeats);
		SeatPolicy.validateSeatsAreUnlocked(selectedSeats);
		SeatPolicy.validateSeatsBelongToStadium(selectedSeats, performance.getStadium());

		ReservationPolicy.validateReservationTimeLimit(elapsedTime);
	}

	public static void validateReservationTimeLimit(Long elapsedTime) {
		if (elapsedTime > 600L) {
			throw new ReservationTimeLimitExceededException();
		}
	}
}
