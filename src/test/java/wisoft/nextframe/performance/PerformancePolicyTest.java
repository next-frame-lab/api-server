package wisoft.nextframe.performance;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wisoft.nextframe.performance.exception.AdultOnlyPerformanceException;
import wisoft.nextframe.performance.exception.InvalidReservationTimeException;
import wisoft.nextframe.performance.exception.PerformanceAlreadyStartedException;
import wisoft.nextframe.user.User;
import wisoft.nextframe.util.PerformanceFixture;
import wisoft.nextframe.util.UserFixture;

class PerformancePolicyTest {

	@DisplayName("이미 시작된 공연은 예매할 수 없다.")
	@Test
	void throwsIfStarted() {
		final Performance startedPerformance = PerformanceFixture.started();

		assertThatThrownBy(() -> PerformancePolicy.validateNotStarted(startedPerformance))
			.isInstanceOf(PerformanceAlreadyStartedException.class);
	}

	@DisplayName("예매 시간이 되지 않았으면 예매할 수 없다.")
	@Test
	void throwsIfBeforeReservationTime() {
		Performance notYetOpenedPerformance = PerformanceFixture.notYetOpened();

		assertThatThrownBy(() -> PerformancePolicy.validateReservationTime(notYetOpenedPerformance))
			.isInstanceOf(InvalidReservationTimeException.class);
	}

	@DisplayName("예매 시간이 지나면 예매할 수 없다.")
	@Test
	void throwsIfAfterReservationTime() {
		Performance endedPerformance = PerformanceFixture.ended();

		assertThatThrownBy(() -> PerformancePolicy.validateReservationTime(endedPerformance))
			.isInstanceOf(InvalidReservationTimeException.class);
	}

	@DisplayName("미성년자는 성인 전용 공연을 예매할 수 없다.")
	@Test
	void throwsIfUnderage() {
		User underage = UserFixture.underage();
		Performance adultOnly = PerformanceFixture.adultOnly();

		assertThatThrownBy(() -> PerformancePolicy.validateAdultOnly(underage, adultOnly))
			.isInstanceOf(AdultOnlyPerformanceException.class);
	}
}