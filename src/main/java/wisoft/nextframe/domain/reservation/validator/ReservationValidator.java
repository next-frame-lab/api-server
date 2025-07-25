package wisoft.nextframe.domain.reservation.validator;

import java.util.Set;

import wisoft.nextframe.domain.performance.Performance;
import wisoft.nextframe.domain.reservation.ElapsedTime;
import wisoft.nextframe.domain.reservation.exception.ReservationTimeLimitExceededException;
import wisoft.nextframe.domain.seat.Seat;
import wisoft.nextframe.domain.seat.validator.SeatValidator;
import wisoft.nextframe.domain.user.User;
import wisoft.nextframe.domain.performance.validator.PerformanceValidator;

public class ReservationValidator {

	private final PerformanceValidator performanceValidator;
	private final SeatValidator seatValidator;

	public ReservationValidator(PerformanceValidator performanceValidator, SeatValidator seatValidator) {
		this.performanceValidator = performanceValidator;
		this.seatValidator = seatValidator;
	}

	public void validate(User user, Performance performance, Set<Seat> seats, ElapsedTime elapsedTime) {
		performanceValidator.validate(user, performance);
		seatValidator.validate(seats, performance.getStadium());
		validateReservationTimeLimit(elapsedTime);
	}

	private void validateReservationTimeLimit(ElapsedTime elapsedTime) {
		if (elapsedTime.isExceeded()) {
			throw new ReservationTimeLimitExceededException();
		}
	}
}
