package wisoft.nextframe.schedulereservationticketing.schedule.domain.performance.validator;

import wisoft.nextframe.schedulereservationticketing.schedule.domain.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.schedule.domain.performance.exception.AdultOnlyPerformanceException;
import wisoft.nextframe.schedulereservationticketing.schedule.domain.schedule.exception.InvalidReservablePeriodException;
import wisoft.nextframe.schedulereservationticketing.schedule.domain.schedule.exception.PerformanceAlreadyStartedException;
import wisoft.nextframe.schedulereservationticketing.user.domain.User;

public class PerformanceValidator {

	public void validate(User user, Performance performance) {
		validateAdultOnlyPerformance(user, performance);
		validateNotStarted(performance);
		validateReservablePeriod(performance);
	}

	private void validateAdultOnlyPerformance(User user, Performance performance) {
		if (!performance.isReservableBy(user)) {
			throw new AdultOnlyPerformanceException();
		}
	}

	private void validateNotStarted(Performance performance) {
		if (performance.isStarted()) {
			throw new PerformanceAlreadyStartedException();
		}
	}

	public void validateReservablePeriod(Performance performance) {
		if (!performance.isReservableNow()) {
			throw new InvalidReservablePeriodException();
		}
	}
}
