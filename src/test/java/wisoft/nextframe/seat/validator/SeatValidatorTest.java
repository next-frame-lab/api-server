package wisoft.nextframe.seat.validator;

import static org.assertj.core.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wisoft.nextframe.domain.seat.Seat;
import wisoft.nextframe.domain.seat.validator.SeatValidator;
import wisoft.nextframe.domain.seat.exception.InvalidSeatForStadiumException;
import wisoft.nextframe.domain.seat.exception.NoSeatSelectedException;
import wisoft.nextframe.domain.seat.exception.SeatAlreadyLockedException;
import wisoft.nextframe.domain.seat.exception.TooManySeatsSelectedException;
import wisoft.nextframe.domain.stadium.Stadium;
import wisoft.nextframe.util.SeatFixture;
import wisoft.nextframe.util.StadiumFixture;

class SeatValidatorTest {

	private final SeatValidator validator = new SeatValidator();

	@DisplayName("좌석이 하나도 선택되지 않으면 예외가 발생한다")
	@Test
	void validate_emptySeats_throwsException() {
		// given
		final Set<Seat> seats = SeatFixture.empty();
		final Stadium stadium = StadiumFixture.defaultStadium();

		// when and then
		assertThatThrownBy(() -> validator.validate(seats, stadium))
			.isInstanceOf(NoSeatSelectedException.class);
	}

	@DisplayName("좌석이 4개 초과면 예외가 발생한다")
	@Test
	void validate_tooManySeats_throwsException() {
		// given
		Set<Seat> seats = SeatFixture.ofCount(6);
		Stadium stadium = StadiumFixture.defaultStadium();

		// when and then
		assertThatThrownBy(() -> validator.validate(seats, stadium))
			.isInstanceOf(TooManySeatsSelectedException.class);
	}

	@DisplayName("잠긴 좌석이 포함돼 있으면 예외가 발생한다")
	@Test
	void validate_containsLockedSeat_throwsException() {
		// given
		Set<Seat> seats = SeatFixture.mixedAvailableAndLocked();
		Stadium stadium = StadiumFixture.defaultStadium();

		// when and then
		assertThatThrownBy(() -> validator.validate(seats, stadium))
			.isInstanceOf(SeatAlreadyLockedException.class);
	}

	@DisplayName("공연장에 포함되지 않은 좌석이 있으면 예외가 발생한다")
	@Test
	void validate_seatNotInStadium_throwsException() {
		// given
		Set<Seat> seats = SeatFixture.notInStadium(); // 공연장에 없는 좌석 포함
		Stadium stadium = StadiumFixture.defaultStadium();

		// when and then
		assertThatThrownBy(() -> validator.validate(seats, stadium))
			.isInstanceOf(InvalidSeatForStadiumException.class);
	}

	@DisplayName("모든 조건을 만족하면 예외 없이 통과된다")
	@Test
	void validate_validSeats_passes() {
		// given
		Set<Seat> seats = SeatFixture.validInStadium();
		Stadium stadium = StadiumFixture.createWithSeats(seats);

		// when and then
		assertThatCode(() -> validator.validate(seats, stadium))
			.doesNotThrowAnyException();
	}
}
