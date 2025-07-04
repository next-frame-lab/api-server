package wisoft.nextframe.reservation;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import wisoft.nextframe.performance.Performance;
import wisoft.nextframe.performance.exception.AdultOnlyPerformanceException;
import wisoft.nextframe.performance.exception.InvalidReservationTimeException;
import wisoft.nextframe.performance.exception.PerformanceAlreadyStartedException;
import wisoft.nextframe.reservation.exception.ReservationTimeLimitExceededException;
import wisoft.nextframe.seat.Seat;
import wisoft.nextframe.seat.exception.InvalidSeatForStadiumException;
import wisoft.nextframe.seat.exception.NoSeatSelectedException;
import wisoft.nextframe.seat.exception.SeatAlreadyLockedException;
import wisoft.nextframe.seat.exception.TooManySeatsSelectedException;
import wisoft.nextframe.stadium.Stadium;
import wisoft.nextframe.user.User;
import wisoft.nextframe.util.PerformanceFixture;
import wisoft.nextframe.util.ReservationFixture;
import wisoft.nextframe.util.SeatFixture;
import wisoft.nextframe.util.StadiumFixture;
import wisoft.nextframe.util.UserFixture;

class ReservationTest {

	@Nested
	@DisplayName("예매 성공 케이스")
	class SuccessCases {

		@Test
		@DisplayName("성인은 성인 전용 공연을 예매할 수 있다.")
		void canReserve_adultOnlyPerformance_whenUserIsAdult() {
			User adult = UserFixture.adult();
			Performance adultOnlyPerformance = PerformanceFixture.adultOnly();

			Reservation reservation = ReservationFixture.createWithUserAndPerformance(adult, adultOnlyPerformance);

			assertThat(reservation.getUser()).isEqualTo(adult);
			assertThat(reservation.getPerformance()).isEqualTo(adultOnlyPerformance);
			assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CREATED);
		}

		@Test
		@DisplayName("1~4개의 좌석을 선택하면 예매에 성공한다.")
		void canReserve_whenSeatCountIsWithinLimit() {
			Seat seat1 = SeatFixture.available("VIP", "A", 1);
			Seat seat2 = SeatFixture.available("일반", "B", 1);
			Set<Seat> seats = Set.of(seat1, seat2);

			Reservation reservation = ReservationFixture.createWithSeats(seats);

			assertThat(reservation.getReservedSeats()).hasSize(2);
		}

		@Test
		@DisplayName("예매가 생성되면 상태는 CREATED가 된다.")
		void reservationStatusIsCreated_whenReservationIsCreated() {
			Reservation reservation = ReservationFixture.defaultReservation();

			assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CREATED);
		}

		@Test
		@DisplayName("예매가 취소되면 상태는 CANCELED가 된다.")
		void reservationStatusIsCanceled_whenReservationIsCanceled() {
			Reservation reservation = ReservationFixture.defaultReservation();

			reservation.changeStatusTo(TransitionType.CANCEL);

			assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
		}

		@Test
		@DisplayName("예매가 확정되면 상태는 CONFIRMED가 된다.")
		void reservationStatusIsConfirmed_whenReservationIsConfirmed() {
			Reservation reservation = ReservationFixture.defaultReservation();

			reservation.changeStatusTo(TransitionType.CONFIRM);

			assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
		}

		@Test
		@DisplayName("예매가 생성되면 좌석이 예매(lock)된다.")
		void seatsAreLocked_whenReservationIsCreated() {
			Seat seat1 = SeatFixture.available("VIP", "A", 1);
			Seat seat2 = SeatFixture.available("일반", "B", 1);
			Set<Seat> selectedSeats = Set.of(seat1, seat2);

			Reservation reservation = ReservationFixture.createWithSeats(selectedSeats);

			assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CREATED);
			assertThat(selectedSeats).allMatch(Seat::isLocked);
		}

		@Test
		@DisplayName("예매가 취소되면 좌석은 원상 복구(unlock)된다.")
		void seatsAreUnlocked_whenReservationIsCanceled() {
			Seat seat1 = SeatFixture.available("VIP", "A", 1);
			Seat seat2 = SeatFixture.available("일반", "B", 1);
			Set<Seat> selectedSeats = Set.of(seat1, seat2);
			Reservation reservation = ReservationFixture.createWithSeats(selectedSeats);

			reservation.changeStatusTo(TransitionType.CANCEL);

			assertThat(selectedSeats).allMatch(seat -> !seat.isLocked());
		}

		@Test
		@DisplayName("공연장 좌석 가격과 공연 가격을 기반으로 결제 금액을 계산한다.")
		void calculateReservationPrice_BaseOn_StadiumSeat_PerformanceBasePrice() {
			Map<String, Integer> sectionPrice = Map.of("VIP", 20_000, "일반", 0);
			Stadium stadium = StadiumFixture.createWith(sectionPrice);
			int basePrice = 130_000;
			Performance performance = PerformanceFixture.createWith(basePrice, stadium);

			Set<Seat> seats = Set.of(
				SeatFixture.available("VIP", "A", 1),
				SeatFixture.available("일반", "B", 1)
			);

			Reservation reservation = ReservationFixture.createWithPerformanceAndSeats(performance, seats);
			int totalPrice = reservation.calculateReservationPrice();

			assertThat(totalPrice).isEqualTo((130_000 + 20_000) + 130_000);
		}
	}

	@Nested
	@DisplayName("예매 실패 케이스")
	class FailureCases {

		@Test
		@DisplayName("성인이 아닌 사용자는 성인 전용 공연을 예매할 수 없다.")
		void cannotReserve_adultOnlyPerformance_whenUserIsUnderage() {
			User underage = UserFixture.underage();
			Performance adultOnlyPerformance = PerformanceFixture.adultOnly();

			assertThatThrownBy(() -> ReservationFixture.createWithUserAndPerformance(underage, adultOnlyPerformance))
				.isInstanceOf(AdultOnlyPerformanceException.class);
		}

		@Test
		@DisplayName("좌석을 선택하지 않으면 예매할 수 없다.")
		void cannotReserve_whenNoSeatSelected() {
			Set<Seat> noSeat = SeatFixture.none();

			assertThatThrownBy(() -> ReservationFixture.createWithSeats(noSeat))
				.isInstanceOf(NoSeatSelectedException.class);
		}

		@Test
		@DisplayName("좌석을 4개를 초과해서 선택하면 예매할 수 없다.")
		void cannotReserve_whenSeatCountExceedsLimit() {
			Set<Seat> exceededSeats = SeatFixture.exceedingLimitSeats();

			assertThatThrownBy(() -> ReservationFixture.createWithSeats(exceededSeats))
				.isInstanceOf(TooManySeatsSelectedException.class);
		}

		@Test
		@DisplayName("이미 예약(lock)된 좌석을 선택하면 예매할 수 없다.")
		void cannotReserve_whenSeatIsAlreadyLocked() {
			Set<Seat> lockedSeats = SeatFixture.lockedSeats();

			assertThatThrownBy(() -> ReservationFixture.createWithSeats(lockedSeats))
				.isInstanceOf(SeatAlreadyLockedException.class);
		}

		@Test
		@DisplayName("공연 시작 이후에는 예매할 수 없다.")
		void cannotReserve_whenPerformanceStarted() {
			Performance startedPerformance = PerformanceFixture.started();

			assertThatThrownBy(() -> ReservationFixture.createWithPerformance(startedPerformance))
				.isInstanceOf(PerformanceAlreadyStartedException.class);
		}

		@Test
		@DisplayName("사용자가 예매 시작 후 10분이 지나면 예매할 수 없다.")
		void cannotReserve_whenElapsedTimeExceed10Minutes() {
			Long elapsedTime = 601L;

			assertThatThrownBy(() -> ReservationFixture.createWithElapsedTime(elapsedTime))
				.isInstanceOf(ReservationTimeLimitExceededException.class);
		}

		@Test
		@DisplayName("공연장에 존재하지 않은 좌석을 선택하면 예매할 수 없다.")
		void cannotReserve_whenSeatNotInStadium() {
			Set<Seat> invalidSeats = Set.of(SeatFixture.available("VIP", "Z", 99));

			assertThatThrownBy(() -> ReservationFixture.createWithSeats(invalidSeats))
				.isInstanceOf(InvalidSeatForStadiumException.class);
		}

		@Test
		@DisplayName("예매 가능한 시간이 아니면 예매할 수 없다.")
		void cannotReserve_whenNotInReservationTime() {
			LocalDateTime startTime = LocalDateTime.now().plusHours(1);
			LocalDateTime endTime = LocalDateTime.now().plusDays(1);
			Performance performance = PerformanceFixture.createWith(startTime, endTime);

			assertThatThrownBy(() -> ReservationFixture.createWithPerformance(performance))
				.isInstanceOf(InvalidReservationTimeException.class);
		}
	}
}

