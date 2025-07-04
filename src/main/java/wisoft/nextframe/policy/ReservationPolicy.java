package wisoft.nextframe.policy;

import java.time.Duration;

public class ReservationPolicy {

	public static void validate(Long elapsedTime) {
		validateReservationTimeLimit(elapsedTime);
	}

	private static void validateReservationTimeLimit(Long elapsedTime) {
		if (Duration.ofSeconds(elapsedTime).toSeconds() > 600) {
			throw new IllegalArgumentException("예매 시작 후 10분이 지나면 예매할 수 없습니다.");
		}
	}
}
