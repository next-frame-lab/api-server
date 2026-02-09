package wisoft.nextframe.schedulereservationticketing.service.reservation;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import wisoft.nextframe.schedulereservationticketing.entity.reservation.Reservation;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.ReservationSeat;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.ReservationSeatId;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.seat.SeatState;
import wisoft.nextframe.schedulereservationticketing.repository.seat.SeatStateRepository;

@ExtendWith(MockitoExtension.class)
class ReservationTimeoutCancellerTest {

	@InjectMocks
	private ReservationTimeoutCanceller reservationTimeoutCanceller;

	@Mock
	private SeatStateRepository seatStateRepository;
	@Mock
	private TransactionTemplate transactionTemplate;

	@Test
	@DisplayName("만료된 예약이 없으면 0을 반환하고 아무 작업도 하지 않는다")
	void cancelExpiredReservations_noExpired_returnsZero() {
		// given
		LocalDateTime now = LocalDateTime.now();
		given(transactionTemplate.execute(any(TransactionCallback.class)))
			.willReturn(Collections.emptyList());

		// when
		int result = reservationTimeoutCanceller.cancelExpiredReservations(now);

		// then
		assertThat(result).isZero();
		verifyNoInteractions(seatStateRepository);
	}

	@Test
	@DisplayName("만료된 예약이 있으면 예약을 취소하고 좌석 잠금을 해제한다")
	void cancelExpiredReservations_withExpired_cancelsAndUnlocksSeats() {
		// given
		LocalDateTime now = LocalDateTime.now();
		UUID scheduleId = UUID.randomUUID();
		UUID seatId1 = UUID.randomUUID();
		UUID seatId2 = UUID.randomUUID();

		Schedule mockSchedule = mock(Schedule.class);
		given(mockSchedule.getId()).willReturn(scheduleId);

		ReservationSeat mockSeat1 = mock(ReservationSeat.class);
		ReservationSeat mockSeat2 = mock(ReservationSeat.class);
		given(mockSeat1.getId()).willReturn(new ReservationSeatId(UUID.randomUUID(), seatId1));
		given(mockSeat2.getId()).willReturn(new ReservationSeatId(UUID.randomUUID(), seatId2));

		Reservation mockReservation = mock(Reservation.class);
		given(mockReservation.getReservationSeats()).willReturn(List.of(mockSeat1, mockSeat2));
		given(mockReservation.getSchedule()).willReturn(mockSchedule);

		given(transactionTemplate.execute(any(TransactionCallback.class)))
			.willReturn(List.of(mockReservation));
		willAnswer(invocation -> {
			invocation.getArgument(0, Consumer.class).accept(null);
			return null;
		}).given(transactionTemplate).executeWithoutResult(any());

		SeatState mockSeatState1 = mock(SeatState.class);
		SeatState mockSeatState2 = mock(SeatState.class);
		given(seatStateRepository.findByScheduleIdAndSeatIds(scheduleId, List.of(seatId1, seatId2)))
			.willReturn(List.of(mockSeatState1, mockSeatState2));

		// when
		int result = reservationTimeoutCanceller.cancelExpiredReservations(now);

		// then
		assertThat(result).isEqualTo(1);
		verify(mockReservation).cancel();
		verify(mockSeatState1).unlock();
		verify(mockSeatState2).unlock();
	}

	@Test
	@DisplayName("만료된 예약이 여러 건이면 모두 취소하고 처리 건수를 반환한다")
	void cancelExpiredReservations_multipleExpired_cancelsAll() {
		// given
		LocalDateTime now = LocalDateTime.now();

		UUID scheduleId1 = UUID.randomUUID();
		UUID scheduleId2 = UUID.randomUUID();
		UUID seatId1 = UUID.randomUUID();
		UUID seatId2 = UUID.randomUUID();

		// 첫 번째 예약
		Schedule mockSchedule1 = mock(Schedule.class);
		given(mockSchedule1.getId()).willReturn(scheduleId1);

		ReservationSeat mockRsSeat1 = mock(ReservationSeat.class);
		given(mockRsSeat1.getId()).willReturn(new ReservationSeatId(UUID.randomUUID(), seatId1));

		Reservation mockReservation1 = mock(Reservation.class);
		given(mockReservation1.getReservationSeats()).willReturn(List.of(mockRsSeat1));
		given(mockReservation1.getSchedule()).willReturn(mockSchedule1);

		// 두 번째 예약
		Schedule mockSchedule2 = mock(Schedule.class);
		given(mockSchedule2.getId()).willReturn(scheduleId2);

		ReservationSeat mockRsSeat2 = mock(ReservationSeat.class);
		given(mockRsSeat2.getId()).willReturn(new ReservationSeatId(UUID.randomUUID(), seatId2));

		Reservation mockReservation2 = mock(Reservation.class);
		given(mockReservation2.getReservationSeats()).willReturn(List.of(mockRsSeat2));
		given(mockReservation2.getSchedule()).willReturn(mockSchedule2);

		given(transactionTemplate.execute(any(TransactionCallback.class)))
			.willReturn(List.of(mockReservation1, mockReservation2));
		willAnswer(invocation -> {
			invocation.getArgument(0, Consumer.class).accept(null);
			return null;
		}).given(transactionTemplate).executeWithoutResult(any());

		SeatState mockSeatState1 = mock(SeatState.class);
		SeatState mockSeatState2 = mock(SeatState.class);
		given(seatStateRepository.findByScheduleIdAndSeatIds(scheduleId1, List.of(seatId1)))
			.willReturn(List.of(mockSeatState1));
		given(seatStateRepository.findByScheduleIdAndSeatIds(scheduleId2, List.of(seatId2)))
			.willReturn(List.of(mockSeatState2));

		// when
		int result = reservationTimeoutCanceller.cancelExpiredReservations(now);

		// then
		assertThat(result).isEqualTo(2);
		verify(mockReservation1).cancel();
		verify(mockReservation2).cancel();
		verify(mockSeatState1).unlock();
		verify(mockSeatState2).unlock();
	}

	@Test
	@DisplayName("동시 결제로 OptimisticLockException 발생 시 해당 예약을 건너뛰고 나머지를 처리한다")
	void cancelExpiredReservations_optimisticLockConflict_skipsAndContinues() {
		// given
		LocalDateTime now = LocalDateTime.now();

		Reservation conflictReservation = mock(Reservation.class);
		given(conflictReservation.getId()).willReturn(UUID.randomUUID());

		Reservation normalReservation = mock(Reservation.class);
		UUID scheduleId = UUID.randomUUID();
		UUID seatId = UUID.randomUUID();

		Schedule mockSchedule = mock(Schedule.class);
		given(mockSchedule.getId()).willReturn(scheduleId);

		ReservationSeat mockRsSeat = mock(ReservationSeat.class);
		given(mockRsSeat.getId()).willReturn(new ReservationSeatId(UUID.randomUUID(), seatId));

		given(normalReservation.getReservationSeats()).willReturn(List.of(mockRsSeat));
		given(normalReservation.getSchedule()).willReturn(mockSchedule);

		given(transactionTemplate.execute(any(TransactionCallback.class)))
			.willReturn(List.of(conflictReservation, normalReservation));

		// 첫 번째 호출: OptimisticLockException, 두 번째 호출: 정상 실행
		willThrow(new ObjectOptimisticLockingFailureException(Reservation.class.getName(), null))
			.willAnswer(invocation -> {
				invocation.getArgument(0, Consumer.class).accept(null);
				return null;
			}).given(transactionTemplate).executeWithoutResult(any());

		SeatState mockSeatState = mock(SeatState.class);
		given(seatStateRepository.findByScheduleIdAndSeatIds(scheduleId, List.of(seatId)))
			.willReturn(List.of(mockSeatState));

		// when
		int result = reservationTimeoutCanceller.cancelExpiredReservations(now);

		// then
		assertThat(result).isEqualTo(1);
		verify(conflictReservation, never()).cancel();
		verify(normalReservation).cancel();
		verify(mockSeatState).unlock();
	}
}
