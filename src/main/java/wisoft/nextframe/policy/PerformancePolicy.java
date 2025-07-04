package wisoft.nextframe.policy;

import wisoft.nextframe.performance.Performance;
import wisoft.nextframe.user.User;

public class PerformancePolicy {

	public static void validate(User user, Performance performance) {
		validateAdultOnly(user, performance);
		validateNotStarted(performance);
		validateReservationTime(performance);
	}

	private static void validateAdultOnly(User user, Performance performance) {
		if (!performance.isReservableBy(user)) {
			throw new IllegalArgumentException("성인 전용 공연은 성인만 예매할 수 있습니다.");
		}
	}

	private static void validateNotStarted(Performance performance) {
		if (performance.isStarted()) {
			throw new IllegalArgumentException("이미 시작된 공연은 예매할 수 없습니다.");
		}
	}

	private static void validateReservationTime(Performance performance) {
		if (!performance.isReservableNow()) {
			throw new IllegalArgumentException("예매 가능한 시간이 아닙니다.");
		}
	}
}
