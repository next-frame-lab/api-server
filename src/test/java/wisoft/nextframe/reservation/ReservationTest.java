package wisoft.nextframe.reservation;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wisoft.nextframe.performance.Performance;
import wisoft.nextframe.seat.Seat;
import wisoft.nextframe.stadium.Stadium;
import wisoft.nextframe.user.User;
import wisoft.nextframe.util.PerformanceFixture;
import wisoft.nextframe.util.ReservationFixture;
import wisoft.nextframe.util.SeatFixture;
import wisoft.nextframe.util.StadiumFixture;
import wisoft.nextframe.util.UserFixture;

class ReservationTest {

	@Test
	@DisplayName("성인이 아닌 사용자는 성인 전용 공연을 예매할 수 없다.")
	void cannotReserve_adultOnlyPerformance_whenUserIsUnderage() {
		// given
		User underage = UserFixture.underage();
		Performance adultOnlyPerformance = PerformanceFixture.adultOnly();

		// when and then
		assertThatThrownBy(() -> ReservationFixture.createWith(underage, adultOnlyPerformance))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("성인 전용 공연은 성인만 예매할 수 있습니다.");
	}

	@Test
	@DisplayName("성인은 성인 전용 공연을 예매할 수 있다.")
	void canReserve_adultOnlyPerformance_whenUserIsAdult() {
		// given
		User adult = UserFixture.adult();
		Performance adultOnlyPerformance = PerformanceFixture.adultOnly();

		// when
		Reservation reservation = ReservationFixture.createWith(adult, adultOnlyPerformance);

		// then
		assertThat(reservation.getUser()).isEqualTo(adult);
		assertThat(reservation.getPerformance()).isEqualTo(adultOnlyPerformance);
		assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CREATED);
	}

	@Test
	@DisplayName("좌석을 선택하지 않으면 예매할 수 없다.")
	void cannotReserve_whenNoSeatSelected() {
		// given
		Set<Seat> noSeat = SeatFixture.none();

		// when and then
		assertThatThrownBy(() -> ReservationFixture.createWith(noSeat))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("좌석은 최소 1개 이상 선택해야 합니다.");
	}

	@Test
	@DisplayName("좌석을 4개를 초과해서 선택하면 예매할 수 없다.")
	void cannotReserve_whenSeatCountExceedsLimit() {
		// given
		Set<Seat> exceededSeats = SeatFixture.exceedingLimitSeats();

		// when and then
		assertThatThrownBy(() -> ReservationFixture.createWith(exceededSeats))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("좌석은 최대 4개까지만 선택할 수 있습니다.");
	}

	@Test
	@DisplayName("1~4개의 좌석을 선택하면 예매에 성공한다.")
	void canReserve_whenSeatCountIsWithinLimit() {
		// given
		Seat seat1 = SeatFixture.available("VIP", "A", 1);
		Seat seat2 = SeatFixture.available("일반", "B", 1);
		Set<Seat> seats = Set.of(seat1, seat2);

		// when
		Reservation reservation = ReservationFixture.createWith(seats);

		// then
		assertThat(reservation.getReservedSeats()).hasSize(2);
	}

	@Test
	@DisplayName("이미 예약(lock)된 좌석을 선택하면 예매할 수 없다.")
	void cannotReserve_whenSeatIsAlreadyLocked() {
		// given
		Set<Seat> lockedSeats = SeatFixture.lockedSeats();

		// when and then
		assertThatThrownBy(() -> ReservationFixture.createWith(lockedSeats))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("이미 예약된 좌석은 선택할 수 없습니다.");
	}

	@Test
	@DisplayName("공연 시작 이후에는 예매할 수 없다.")
	void cannotReserve_whenPerformanceStarted() {
		// given
		Performance startedPerformance = PerformanceFixture.started();

		// when and then
		assertThatThrownBy(() -> ReservationFixture.createWith(startedPerformance))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("이미 시작된 공연은 예매할 수 없습니다.");
	}

	@Test
	@DisplayName("사용자가 예매 시작 후 10분이 지나면 예매할 수 없다.")
	void cannotReserve_whenElapsedTimeExceed10Minutes() {
		// given
		Long elapsedTime = 601L; // 10분이 지난 시간

		// when and then
		assertThatThrownBy(() -> ReservationFixture.createWith(elapsedTime))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("예매 시작 후 10분이 지나면 예매할 수 없습니다.");
	}

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
	void reservationStatusIsCanceled_whenReservationIsCanceled() {
		// given
		Reservation reservation = ReservationFixture.defaultReservation();

		// when
		reservation.changeStatusTo(TransitionType.CANCEL);

		// then
		assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
	}

	@Test
	@DisplayName("예매가 확정되면 상태는 CONFIRMED가 된다.")
	void reservationStatusIsConfirmed_whenReservationIsConfirmed() {
		// given
		Reservation reservation = ReservationFixture.defaultReservation();

		// when
		reservation.changeStatusTo(TransitionType.CONFIRM);

		// then
		assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
	}

	@Test
	@DisplayName("예매가 생성되면 좌석이 예매(lock)된다.")
	void seatsAreLocked_whenReservationIsCreated() {
		// given
		Seat seat1 = SeatFixture.available("VIP", "A", 1);
		Seat seat2 = SeatFixture.available("일반", "B", 1);
		Set<Seat> selectedSeats = Set.of(seat1, seat2);

		// when
		Reservation reservation = ReservationFixture.createWith(selectedSeats);

		// then
		assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CREATED);
		assertThat(selectedSeats).allMatch(Seat::isLocked);
	}

	@Test
	@DisplayName("예매가 취소되면 좌석은 원상 복구(unlock)된다.")
	void seatsAreUnlocked_whenReservationIsCanceled() {
		// given
		Seat seat1 = SeatFixture.available("VIP", "A", 1);
		Seat seat2 = SeatFixture.available("일반", "B", 1);
		Set<Seat> selectedSeats = Set.of(seat1, seat2);
		Reservation reservation = ReservationFixture.createWith(selectedSeats);

		// when
		reservation.changeStatusTo(TransitionType.CANCEL); // 예매 취소

		// then
		assertThat(selectedSeats).allMatch(seat -> !seat.isLocked());
	}

	@Test
	@DisplayName("공연장 좌석 가격과 공연 가격을 기반으로 결제 금액을 계산한다.")
	void calculateReservationPrice_BaseOn_StadiumSeat_PerformanceBasePrice() {
		// given
		Map<String, Integer> sectionPrice = Map.of( // 공연장 좌석별 금액
			"VIP", 20_000,
			"일반", 0
		);
		Stadium stadium = StadiumFixture.createWith(sectionPrice);
		int basePrice = 130_000;
		Performance performance = PerformanceFixture.createWith(basePrice, stadium);

		Set<Seat> seats = Set.of(
			SeatFixture.available("VIP", "A", 1),
			SeatFixture.available("일반", "B", 1)
		);

		Reservation reservation = ReservationFixture.createWith(performance, seats);

		// when
		int totalPrice = reservation.calculateReservationPrice();

		// then
		assertThat(totalPrice).isEqualTo((130_000 + 20_000) + 130_000);
	}

	@Test
	@DisplayName("공연장에 존재하지 않은 좌석을 선택하면 예매할 수 없다.")
	void cannotReserve_whenSeatNotInStadium() {
		// given
		Set<Seat> invalidSeats = Set.of(SeatFixture.available("VIP", "Z", 99)); // 존재하지 않은 좌석

		// when and then
		assertThatThrownBy(() -> ReservationFixture.createWith(invalidSeats))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("해당 공연장의 좌석이 아닙니다.");
	}

	@Test
	@DisplayName("예매 가능한 시간이 아니면 예매할 수 없다.")
	void cannotReserve_whenNotInReservationTime() {
		//given
		LocalDateTime startTime = LocalDateTime.now().plusHours(1); // 1시간 뒤부터 예매 시작
		LocalDateTime endTime = LocalDateTime.now().plusDays(1); // 내일까지 예매 가능

		Performance performance = PerformanceFixture.createWith(startTime, endTime);

		// when and then
		assertThatThrownBy(() -> ReservationFixture.createWith(performance))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("예매 가능한 시간이 아닙니다.");
	}
}
