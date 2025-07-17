package wisoft.nextframe.reservation;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wisoft.nextframe.performance.Performance;
import wisoft.nextframe.reservation.exception.ReservationTimeLimitExceededException;
import wisoft.nextframe.seat.Seat;
import wisoft.nextframe.seat.SeatManager;
import wisoft.nextframe.user.User;
import wisoft.nextframe.util.PerformanceFixture;
import wisoft.nextframe.util.ReservationFixture;
import wisoft.nextframe.util.SeatFixture;
import wisoft.nextframe.util.UserFixture;
import wisoft.nextframe.validator.ReservationValidator;

class ReservationServiceTest {

	ReservationValidator validator = mock(ReservationValidator.class);
	TotalPricePolicy pricePolicy = mock(TotalPricePolicy.class);

	@DisplayName("예매 성공 시 Reservation이 생성되고 좌석이 잠긴다")
	@Test
	void createReservation_success_createsReservationAndLocksSeats() {
		// given
		User user = UserFixture.defaultUser();
		Performance performance = PerformanceFixture.defaultPerformance();
		Set<Seat> seats = SeatFixture.validInStadium();
		Long elapsedTime = 100L;

		SeatManager seatManager = new SeatManager();
		ReservationService service = new ReservationService(validator, seatManager, pricePolicy);

		// when
		Reservation reservation = service.createReservation(user, performance, seats, elapsedTime);

		// then
		assertThat(reservation).isNotNull();
		assertThat(seats).allMatch(Seat::isLocked);
	}

	@DisplayName("검증 예외 발생 시 좌석은 잠기지 않는다")
	@Test
	void createReservation_validationFails_doesNotLockSeats() {
		// given
		User user = UserFixture.adult();
		Performance performance = PerformanceFixture.defaultPerformance();
		Set<Seat> seats = SeatFixture.validInStadium();
		Long elapsedTime = 601L;

		SeatManager seatManager = mock(SeatManager.class);
		ReservationService service = new ReservationService(validator, seatManager, pricePolicy);

		doThrow(new ReservationTimeLimitExceededException())
			.when(validator).validate(user, performance, seats, elapsedTime);

		// when and then
		assertThatThrownBy(() -> service.createReservation(user, performance, seats, elapsedTime))
			.isInstanceOf(ReservationTimeLimitExceededException.class);

		verify(seatManager, never()).lockSeats(any());
	}

	@DisplayName("예매 취소 시 좌석은 잠금 해제된다.")
	@Test
	void unlockSeats_whenReservationIsCanceled() {
		// given
		Set<Seat> seats = Set.of(
			SeatFixture.locked("A", 1, 1),
			SeatFixture.locked("A", 1, 2)
		);
		Reservation reservation = ReservationFixture.withSeats(seats);

		SeatManager seatManager = new SeatManager();
		ReservationService service = new ReservationService(validator, seatManager, pricePolicy);

		// when
		service.cancelReservation(reservation);

		// then
		assertThat(seats).allMatch(seat -> !seat.isLocked());
	}
}
