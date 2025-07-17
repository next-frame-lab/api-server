package wisoft.nextframe.validator;

import wisoft.nextframe.performance.Performance;
import wisoft.nextframe.performance.exception.AdultOnlyPerformanceException;
import wisoft.nextframe.performance.exception.InvalidReservablePeriodException;
import wisoft.nextframe.performance.exception.PerformanceAlreadyStartedException;
import wisoft.nextframe.user.User;

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
