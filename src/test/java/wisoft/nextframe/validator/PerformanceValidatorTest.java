package wisoft.nextframe.validator;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wisoft.nextframe.domain.performance.Performance;
import wisoft.nextframe.domain.performance.exception.AdultOnlyPerformanceException;
import wisoft.nextframe.domain.performance.exception.InvalidReservablePeriodException;
import wisoft.nextframe.domain.performance.exception.PerformanceAlreadyStartedException;
import wisoft.nextframe.domain.user.User;
import wisoft.nextframe.util.PerformanceFixture;
import wisoft.nextframe.util.UserFixture;

class PerformanceValidatorTest {

	private final PerformanceValidator validator = new PerformanceValidator();

	@DisplayName("성인 전용 공연은 미성년자는 예외를 발생시킨다")
	@Test
	void validate_underage_throwsException() {
		// given
		final User underage = UserFixture.underage();
		final Performance adultOnly = PerformanceFixture.adultOnlyPerformance();

		// when and then
		assertThatThrownBy(() -> validator.validate(underage, adultOnly))
			.isInstanceOf(AdultOnlyPerformanceException.class);
	}

	@DisplayName("성인 전용 공연은 성인이면 예외 없이 통과된다")
	@Test
	void validate_adult_passes() {
		// given
		final User adult = UserFixture.adult();
		final Performance adultOnly = PerformanceFixture.adultOnlyPerformance();

		// when and then
		assertThatCode(() -> validator.validate(adult, adultOnly))
			.doesNotThrowAnyException();
	}

	@DisplayName("이미 시작된 공연은 예매할 수 없어 예외가 발생한다")
	@Test
	void validate_startedPerformance_throwsException() {
		// given
		final User user = UserFixture.defaultUser();
		final Performance started = PerformanceFixture.alreadyStartedPerformance();

		// when and then
		assertThatThrownBy(() -> validator.validate(user, started))
			.isInstanceOf(PerformanceAlreadyStartedException.class);
	}

	@DisplayName("시작 전 공연은 예매가 가능하다")
	@Test
	void validate_notStartedPerformance_passes() {
		// given
		final User user = UserFixture.defaultUser();
		Performance notStarted = PerformanceFixture.defaultPerformance();

		// when and then
		assertThatCode(() -> validator.validate(user, notStarted))
			.doesNotThrowAnyException();
	}

	@DisplayName("예매 가능 기간이 아니면 예외가 발생한다")
	@Test
	void validate_outOfReservablePeriod_throwsException() {
		// given
		User user = UserFixture.defaultUser();
		Performance outOfPeriod = PerformanceFixture.reservationClosedPerformance();

		// when and then
		assertThatThrownBy(() -> validator.validate(user, outOfPeriod))
			.isInstanceOf(InvalidReservablePeriodException.class);
	}

	@DisplayName("예매 가능 기간이면 예외 없이 통과된다")
	@Test
	void validate_withinReservablePeriod_passes() {
		User user = UserFixture.defaultUser();
		Performance withinPeriod = PerformanceFixture.reservationOpenPerformance(); // isReservableNow() == true

		// when and then
		assertThatCode(() -> validator.validate(user, withinPeriod))
			.doesNotThrowAnyException();
	}
}
