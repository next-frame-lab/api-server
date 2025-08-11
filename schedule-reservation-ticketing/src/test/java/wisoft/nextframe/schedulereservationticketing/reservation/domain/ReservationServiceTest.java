package wisoft.nextframe.schedulereservationticketing.reservation.domain;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wisoft.nextframe.schedule.domain.performance.Performance;
import wisoft.nextframe.reservation.domain.exception.ReservationTimeLimitExceededException;
import wisoft.nextframe.schedule.domain.seat.Seat;
import wisoft.nextframe.schedule.domain.seat.SeatManager;
import wisoft.nextframe.user.domain.User;
import wisoft.nextframe.schedulereservationticketing.schedule.domain.fixture.PerformanceFixture;
import wisoft.nextframe.schedulereservationticketing.reservation.domain.fixture.ReservationFixture;
import wisoft.nextframe.schedulereservationticketing.schedule.domain.fixture.SeatFixture;
import wisoft.nextframe.schedulereservationticketing.user.domain.UserFixture;
import wisoft.nextframe.reservation.domain.validator.ReservationValidator;

class ReservationServiceTest {

	ReservationValidator validator = mock(ReservationValidator.class);
	ReservationFactory reservationFactory = new ReservationFactory();

	User user;
	Performance performance;
	Set<Seat> seats;

	@BeforeEach
	void setUp() {
		user = UserFixture.defaultUser();
		performance = PerformanceFixture.defaultPerformance();
		seats = SeatFixture.validInStadium();
	}

	@DisplayName("예매 성공 시 Reservation이 생성되고 좌석이 잠긴다")
	@Test
	void createReservation_success_createsReservationAndLocksSeats() {
		// given
		ElapsedTime elapsedTime = ElapsedTime.of(100L);

		SeatManager seatManager = new SeatManager();
		ReservationService service = new ReservationService(validator, seatManager, reservationFactory);

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
		ElapsedTime elapsedTime = ElapsedTime.of(601L);

		SeatManager seatManager = mock(SeatManager.class);
		ReservationService service = new ReservationService(validator, seatManager, reservationFactory);

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
		Reservation reservation = ReservationFixture.withSeats(seats);

		SeatManager seatManager = new SeatManager();
		ReservationService service = new ReservationService(validator, seatManager, reservationFactory);

		// when
		service.cancelReservation(reservation);

		// then
		assertThat(seats).allMatch(seat -> !seat.isLocked());
	}
}
