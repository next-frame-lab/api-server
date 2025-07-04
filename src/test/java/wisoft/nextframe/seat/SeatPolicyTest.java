package wisoft.nextframe.seat;

import static org.assertj.core.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wisoft.nextframe.seat.exception.InvalidSeatForStadiumException;
import wisoft.nextframe.seat.exception.NoSeatSelectedException;
import wisoft.nextframe.seat.exception.SeatAlreadyLockedException;
import wisoft.nextframe.seat.exception.TooManySeatsSelectedException;
import wisoft.nextframe.stadium.Stadium;
import wisoft.nextframe.util.SeatFixture;
import wisoft.nextframe.util.StadiumFixture;

class SeatPolicyTest {

	@DisplayName("좌석을 선택하지 않으면 예외가 발생한다.")
	@Test
	void throwsIfNoSeatSelected() {
		Set<Seat> emptySeats = SeatFixture.none();

		assertThatThrownBy(() -> SeatPolicy.validateSeatsCount(emptySeats))
			.isInstanceOf(NoSeatSelectedException.class);
	}

	@DisplayName("좌석을 5개 이상 선택하면 예외가 발생한다.")
	@Test
	void throwsIfTooManySeatsSelected() {
		Set<Seat> tooManySeats = SeatFixture.multipleSeats(5);

		assertThatThrownBy(() -> SeatPolicy.validateSeatsCount(tooManySeats))
			.isInstanceOf(TooManySeatsSelectedException.class);
	}

	@DisplayName("좌석을 1~4개 선택하면 예외가 발생하지 않는다.")
	@Test
	void allowIfSeatsCountIsValid() {
		Set<Seat> validSeats = SeatFixture.multipleSeats(4);

		assertThatCode(() -> SeatPolicy.validateSeatsCount(validSeats))
			.doesNotThrowAnyException();
	}

	@DisplayName("이미 예약된 좌석이 하나라도 포함되어 있으면 예외가 발생한다.")
	@Test
	void throwsIfAnySeatIsLocked() {
		HashSet<Seat> seats = new HashSet<>();
		seats.add(SeatFixture.available("VIP", "B", 1));
		seats.add(SeatFixture.locked("VIP", "B", 2));

		assertThatThrownBy(() -> SeatPolicy.validateSeatsAreUnlocked(seats))
			.isInstanceOf(SeatAlreadyLockedException.class);
	}

	@DisplayName("모든 좌석이 예약 가능한 상태라면 예외가 발생하지 않는다.")
	@Test
	void allowIfAllSeatsAreReservable() {
		Set<Seat> seats = Set.of(
			SeatFixture.available("VIP", "B", 1),
			SeatFixture.available("VIP", "B", 2)
		);

		assertThatCode(() -> SeatPolicy.validateSeatsAreUnlocked(seats))
			.doesNotThrowAnyException();
	}

	@DisplayName("공연장의 좌석이 아닌 좌석이 포함되어 있으면 예외가 발생한다.")
	@Test
	void throwsIfAnySeatIsNotInStadium() {
		// 공연장과 공연장 좌석 1개
		Stadium stadium = StadiumFixture.withSeats(Set.of(
			SeatFixture.available("VIP", "A", 1)
		));

		// 공연장에 포함되지 않은 좌석 포함
		Set<Seat> selectedSeats = Set.of(
			SeatFixture.available("VIP", "A", 1), // 존재
			SeatFixture.available("VIP", "A", 2)  // 존재하지 않음
		);

		assertThatThrownBy(() -> SeatPolicy.validateSeatsBelongToStadium(selectedSeats, stadium))
			.isInstanceOf(InvalidSeatForStadiumException.class);
	}

	@DisplayName("모든 좌석이 공연장에 포함되어 있다면 예외가 발생하지 않는다.")
	@Test
	void allowIfAllSeatsInStadium() {
		Stadium stadium = StadiumFixture.withSeats(Set.of(
			SeatFixture.available("VIP", "A", 1),
			SeatFixture.available("VIP", "A", 2))
		);

		Set<Seat> selectedSeats = Set.of(
			SeatFixture.available("VIP", "A", 1),
			SeatFixture.available("VIP", "A", 2)
		);

		assertThatCode(() -> SeatPolicy.validateSeatsBelongToStadium(selectedSeats, stadium))
			.doesNotThrowAnyException();
	}
}
