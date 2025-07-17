package wisoft.nextframe.validator;

import java.util.Set;

import wisoft.nextframe.performance.Performance;
import wisoft.nextframe.reservation.exception.ReservationTimeLimitExceededException;
import wisoft.nextframe.seat.Seat;
import wisoft.nextframe.user.User;

public class ReservationValidator {

	private final PerformanceValidator performanceValidator;
	private final SeatValidator seatValidator;

	public ReservationValidator(PerformanceValidator performanceValidator, SeatValidator seatValidator) {
		this.performanceValidator = performanceValidator;
		this.seatValidator = seatValidator;
	}

	public void validate(User user, Performance performance, Set<Seat> seats, Long elapsedTime) {
		performanceValidator.validate(user, performance);
		seatValidator.validate(seats, performance.getStadium());
		validateReservationTimeLimit(elapsedTime);
	}

	private void validateReservationTimeLimit(Long elapsedTime) {
		if (elapsedTime > 600L) {
			throw new ReservationTimeLimitExceededException();
		}
	}
}
