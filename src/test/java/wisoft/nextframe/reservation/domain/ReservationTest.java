package wisoft.nextframe.reservation;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wisoft.nextframe.reservation.domain.Reservation;
import wisoft.nextframe.reservation.domain.ReservationStatus;
import wisoft.nextframe.reservation.domain.exception.CannotConfirmCanceledReservationException;
import wisoft.nextframe.reservation.domain.exception.ReservationAlreadyCanceledException;
import wisoft.nextframe.reservation.domain.exception.ReservationAlreadyConfirmedException;
import wisoft.nextframe.util.ReservationFixture;

class ReservationTest {

	@Test
	@DisplayName("예매가 생성되면 상태는 CREATED가 된다.")
	void reservationStatusIsCreated_whenReservationIsCreated() {
		// given and when
		Reservation reservation = ReservationFixture.defaultReservation();

		// then
		assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CREATED);
	}

	@Test
	@DisplayName("예매가 취소되면 상태는 CANCELED가 된다.")
	void statusIsCanceled_whenCanceled() {
		// given
		Reservation reservation = ReservationFixture.defaultReservation();

		// when
		reservation.cancel();

		// then
		assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
	}

	@Test
	@DisplayName("예매가 확정되면 상태는 CONFIRMED가 된다.")
	void statusIsConfirmed_whenConfirmed() {
		// given
		Reservation reservation = ReservationFixture.defaultReservation();

		// when
		reservation.confirm();

		// then
		assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
	}

	@DisplayName("이미 확정된 예매는 다시 확정할 수 없다.")
	@Test
	void cannotConfirm_whenAlreadyConfirmed() {
		// given
		Reservation reservation = ReservationFixture.defaultReservation();
		reservation.confirm();

		// when and then
		assertThatThrownBy(reservation::confirm)
			.isInstanceOf(ReservationAlreadyConfirmedException.class);
	}

	@DisplayName("취소된 예매는 확정할 수 없다.")
	@Test
	void cannotConfirm_whenCanceled() {
		// given
		Reservation reservation = ReservationFixture.defaultReservation();
		reservation.cancel();

		// when and then
		assertThatThrownBy(reservation::confirm)
			.isInstanceOf(CannotConfirmCanceledReservationException.class);
	}

	@DisplayName("이미 취소된 예매는 다시 취소할 수 없다.")
	@Test
	void cannotCancel_whenAlreadyCanceled() {
		// given
		Reservation reservation = ReservationFixture.defaultReservation();
		reservation.cancel();

		// when and then
		assertThatThrownBy(reservation::cancel)
			.isInstanceOf(ReservationAlreadyCanceledException.class);
	}
}
