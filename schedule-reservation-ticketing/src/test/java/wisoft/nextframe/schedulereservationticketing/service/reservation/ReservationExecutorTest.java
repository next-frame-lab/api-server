package wisoft.nextframe.schedulereservationticketing.service.reservation;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.response.ReservationResponse;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.Reservation;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.seat.SeatState;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.StadiumSection;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.repository.reservation.ReservationRepository;
import wisoft.nextframe.schedulereservationticketing.repository.seat.SeatStateRepository;

@ExtendWith(MockitoExtension.class)
class ReservationExecutorTest {

	@InjectMocks
	private ReservationExecutor reservationExecutor;

	@Mock
	private SeatStateRepository seatStateRepository;

	@Mock
	private ReservationRepository reservationRepository;

	@Mock
	private ReservationFactory reservationFactory;

	@Test
	@DisplayName("정상적인 데이터가 주어지면 예매가 성공하고 저장이 수행된다")
	void reserve_success() {
		// given
		int totalAmount = 20000;
		UUID scheduleId = UUID.randomUUID();
		UUID seatId = UUID.randomUUID();

		// 1. Mock 객체 생성
		User mockUser = mock(User.class);
		Performance mockPerformance = mock(Performance.class);
		Schedule mockSchedule = mock(Schedule.class);
		SeatDefinition mockSeat = mock(SeatDefinition.class);

		// 2. 기본 ID 및 데이터 Stubbing
		given(mockSchedule.getId()).willReturn(scheduleId);
		given(mockSeat.getId()).willReturn(seatId);
		given(mockSchedule.getPerformanceDatetime()).willReturn(LocalDateTime.now());

		StadiumSection mockSection = mock(StadiumSection.class);

		given(mockSeat.getStadiumSection()).willReturn(mockSection);
		given(mockSection.getSection()).willReturn("A");

		ReservationContext context = new ReservationContext(
			mockUser, mockSchedule, mockPerformance, List.of(mockSeat)
		);

		// 3. Repository 동작 Stubbing
		SeatState mockSeatState = mock(SeatState.class);
		given(seatStateRepository.findByScheduleIdAndSeatIds(eq(scheduleId), anyList()))
			.willReturn(List.of(mockSeatState));

		// 4. Factory 동작 Stubbing
		Reservation mockReservation = mock(Reservation.class);
		given(mockReservation.getId()).willReturn(UUID.randomUUID());
		given(mockReservation.getTotalPrice()).willReturn(totalAmount);

		given(reservationFactory.create(mockUser, mockSchedule, List.of(mockSeat), totalAmount))
			.willReturn(mockReservation);

		// when
		ReservationResponse response = reservationExecutor.reserve(context, totalAmount);

		// then
		assertThat(response).isNotNull();
		assertThat(response.totalAmount()).isEqualTo(totalAmount);

		// 실행 순서 검증 (조회 -> 락 -> 생성 -> 저장)
		InOrder inOrder = inOrder(seatStateRepository, mockSchedule, reservationFactory, reservationRepository);
		// 1. 좌석 상태 조회
		inOrder.verify(seatStateRepository).findByScheduleIdAndSeatIds(eq(scheduleId), anyList());
		// 2. Schedule 도메인 객체에게 락 위임 확인 (가장 중요한 비즈니스 로직)
		inOrder.verify(mockSchedule).lockSeatsForReservation(anyList(), eq(1));
		// 3. 팩토리를 통한 객체 생성
		inOrder.verify(reservationFactory).create(mockUser, mockSchedule, List.of(mockSeat), totalAmount);
		// 4. 저장소 저장
		inOrder.verify(reservationRepository).save(mockReservation);
	}

	@Test
	@DisplayName("좌석 상태가 조회되지 않아 Schedule이 예외를 던지면 저장이 수행되지 않는다")
	void reserve_fail_seat_not_defined() {
		// given
		UUID scheduleId = UUID.randomUUID();
		UUID seatId = UUID.randomUUID();

		// 1. 필수 Mock 객체 생성
		Schedule mockSchedule = mock(Schedule.class);
		SeatDefinition mockSeat = mock(SeatDefinition.class);

		// ID Stubbing
		given(mockSchedule.getId()).willReturn(scheduleId);
		given(mockSeat.getId()).willReturn(seatId);

		ReservationContext context = new ReservationContext(
			mock(User.class), mockSchedule, mock(Performance.class), List.of(mockSeat)
		);

		// 2. Repository 상황 부여: 빈 리스트 반환 (좌석을 못 찾음)
		given(seatStateRepository.findByScheduleIdAndSeatIds(eq(scheduleId), anyList()))
			.willReturn(Collections.emptyList());

		// 3. Schedule 도메인 로직 예외 Stubbing
		// "lockSeatsForReservation 호출 시 개수 불일치로 예외를 던진다"고 설정
		doThrow(new DomainException(ErrorCode.SEAT_NOT_DEFINED))
			.when(mockSchedule).lockSeatsForReservation(anyList(), eq(1));

		// when and then
		// 검증 1: 예상한 에러 코드가 맞는지
		assertThatThrownBy(() -> reservationExecutor.reserve(context, 10000))
			.isInstanceOf(DomainException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.SEAT_NOT_DEFINED);

		// 검증 2: 예외 발생 시 저장이 절대 호출되면 안 됨
		verify(reservationRepository, never()).save(any());

		// 검증 3: 객체 생성 팩토리도 호출되면 안 됨
		verify(reservationFactory, never()).create(any(), any(), anyList(), anyInt());
	}

	@Test
	@DisplayName("이미 락이 걸린 좌석인 경우 예외가 발생하고 저장이 수행되지 않는다")
	void reserve_fail_already_locked() {
		// given
		UUID scheduleId = UUID.randomUUID();
		UUID seatId = UUID.randomUUID();

		Schedule mockSchedule = mock(Schedule.class);
		SeatDefinition mockSeat = mock(SeatDefinition.class);

		// ID Stubbing
		given(mockSchedule.getId()).willReturn(scheduleId);
		given(mockSeat.getId()).willReturn(seatId);

		ReservationContext context = new ReservationContext(
			mock(User.class), mockSchedule, mock(Performance.class), List.of(mockSeat)
		);

		// 2. Repository 상황 부여: 정상적으로 SeatState 목록 반환
		SeatState mockSeatState = mock(SeatState.class);
		given(seatStateRepository.findByScheduleIdAndSeatIds(eq(scheduleId), anyList()))
			.willReturn(List.of(mockSeatState));

		// 3. Schedule 도메인 로직 예외 Stubbing
		// "잠금 시도 중 이미 잠겨있어 예외 발생" 상황 설정
		doThrow(new DomainException(ErrorCode.SEAT_ALREADY_LOCKED))
			.when(mockSchedule).lockSeatsForReservation(anyList(), eq(1));

		// when and then
		assertThatThrownBy(() -> reservationExecutor.reserve(context, 10000))
			.isInstanceOf(DomainException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.SEAT_ALREADY_LOCKED);

		// 검증
		verify(reservationRepository, never()).save(any());
	}
}