package wisoft.nextframe.schedulereservationticketing.reservation.domain.validator;

import java.util.Set;

import wisoft.nextframe.schedulereservationticketing.schedule.domain.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.reservation.domain.ElapsedTime;
import wisoft.nextframe.schedulereservationticketing.reservation.domain.exception.ReservationTimeLimitExceededException;
import wisoft.nextframe.schedulereservationticketing.schedule.domain.seat.Seat;
import wisoft.nextframe.schedulereservationticketing.schedule.domain.seat.validator.SeatValidator;
import wisoft.nextframe.schedulereservationticketing.user.domain.User;
import wisoft.nextframe.schedulereservationticketing.schedule.domain.performance.validator.PerformanceValidator;

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
