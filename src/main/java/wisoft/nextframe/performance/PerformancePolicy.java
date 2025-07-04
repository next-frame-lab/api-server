package wisoft.nextframe.performance;

import wisoft.nextframe.performance.exception.AdultOnlyPerformanceException;
import wisoft.nextframe.performance.exception.InvalidReservationTimeException;
import wisoft.nextframe.performance.exception.PerformanceAlreadyStartedException;
import wisoft.nextframe.user.User;

public class PerformancePolicy {

	public static void validateAdultOnly(User user, Performance performance) {
		if (!performance.isReservableBy(user)) {
			throw new AdultOnlyPerformanceException();
		}
	}

	public static void validateNotStarted(Performance performance) {
		if (performance.isStarted()) {
			throw new PerformanceAlreadyStartedException();
		}
	}

	public static void validateReservationTime(Performance performance) {
		if (!performance.isReservableNow()) {
			throw new InvalidReservationTimeException();
		}
	}
}
