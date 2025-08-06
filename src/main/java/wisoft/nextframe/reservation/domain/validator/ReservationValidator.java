package wisoft.nextframe.reservation.domain.validator;

import java.util.Set;

import wisoft.nextframe.schedule.domain.performance.Performance;
import wisoft.nextframe.reservation.domain.ElapsedTime;
import wisoft.nextframe.reservation.domain.exception.ReservationTimeLimitExceededException;
import wisoft.nextframe.schedule.domain.seat.Seat;
import wisoft.nextframe.schedule.domain.seat.validator.SeatValidator;
import wisoft.nextframe.user.domain.User;
import wisoft.nextframe.schedule.domain.performance.validator.PerformanceValidator;

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
