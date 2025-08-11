package wisoft.nextframe.schedulereservationticketing.reservation.domain.validator;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wisoft.nextframe.schedulereservationticketing.reservation.domain.ElapsedTime;
import wisoft.nextframe.schedulereservationticketing.reservation.domain.exception.ReservationTimeLimitExceededException;
import wisoft.nextframe.schedulereservationticketing.schedule.domain.fixture.PerformanceFixture;
import wisoft.nextframe.schedulereservationticketing.schedule.domain.fixture.SeatFixture;
import wisoft.nextframe.schedulereservationticketing.schedule.domain.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.schedule.domain.performance.validator.PerformanceValidator;
import wisoft.nextframe.schedulereservationticketing.schedule.domain.seat.Seat;
import wisoft.nextframe.schedulereservationticketing.schedule.domain.seat.validator.SeatValidator;
import wisoft.nextframe.schedulereservationticketing.user.domain.User;
import wisoft.nextframe.schedulereservationticketing.user.domain.UserFixture;

class ReservationValidatorTest {

	private ReservationValidator validator;
	private User user;
	private Performance performance;
	private Set<Seat> seats;

	@BeforeEach
	void setUp() {
		PerformanceValidator performanceValidator = mock(PerformanceValidator.class);
		SeatValidator seatValidator = mock(SeatValidator.class);
		validator = new ReservationValidator(performanceValidator, seatValidator);
		user = UserFixture.defaultUser();
		performance = PerformanceFixture.defaultPerformance();
		seats = SeatFixture.validInStadium();
	}

	@DisplayName("예매 시작 후 10분이 지나면 예외가 발생한다")
	@Test
	void validate_elapsedTimeExceeded_throwsException() {
		// given
		ElapsedTime elapsedTime = ElapsedTime.of(601L);

		// when and then
		assertThatThrownBy(() -> validator.validate(user, performance, seats, elapsedTime))
			.isInstanceOf(ReservationTimeLimitExceededException.class);
	}

	@DisplayName("예매 시작 후 10분 이내면 예외 없이 통과된다")
	@Test
	void validate_elapsedTimeWithinLimit_passes() {
		// given
		ElapsedTime elapsedTime = ElapsedTime.of(600L);

		// when and then
		assertThatCode(() -> validator.validate(user, performance, seats, elapsedTime))
			.doesNotThrowAnyException();
	}
}
