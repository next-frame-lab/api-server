package wisoft.nextframe.schedulereservationticketing.service.reservation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
import wisoft.nextframe.schedulereservationticketing.repository.schedule.ScheduleRepository;
import wisoft.nextframe.schedulereservationticketing.repository.seat.SeatStateRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.SeatDefinitionRepository;
import wisoft.nextframe.schedulereservationticketing.repository.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class ReservationExecutorTest {

	@InjectMocks
	private ReservationExecutor reservationExecutor;

	@Mock
	private UserRepository userRepository;
	@Mock private ScheduleRepository scheduleRepository;
	@Mock private SeatDefinitionRepository seatDefinitionRepository;
	@Mock private SeatStateRepository seatStateRepository;
	@Mock private ReservationRepository reservationRepository;
	@Mock private ReservationFactory reservationFactory;
	@Mock private PriceCalculator priceCalculator;

	// --- Common Mocks ---
	@Mock private Schedule mockSchedule;
	@Mock private Performance mockPerformance;
	@Mock private User mockUser;
	@Mock private SeatDefinition mockSeat;
	@Mock private Reservation mockReservation;
	@Mock private StadiumSection mockSection; // DTO 변환용

	@Test
	@DisplayName("유효한 요청 시 조회 -> 검증 -> 락 -> 저장 순서로 실행되고 응답을 반환한다")
	void reserve_success() {
		// given
		UUID userId = UUID.randomUUID();
		UUID scheduleId = UUID.randomUUID();
		UUID performanceId = UUID.randomUUID();
		UUID seatId = UUID.randomUUID();
		int totalAmount = 50000;

		// 1. Mock Behavior Setup (Data Fetching)
		given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
		given(scheduleRepository.findWithPerformanceById(scheduleId)).willReturn(mockSchedule);
		given(seatDefinitionRepository.findWithStadiumSectionByIdIn(anyList())).willReturn(List.of(mockSeat));

		// 2. Mock Behavior Setup (Domain & Validation)
		given(mockSchedule.getPerformance()).willReturn(mockPerformance);
		given(mockPerformance.getId()).willReturn(performanceId);
		given(seatStateRepository.findByScheduleIdAndSeatIds(any(), anyList()))
			.willReturn(List.of(mock(SeatState.class)));
		given(priceCalculator.calculateTotalPrice(any(), anyList())).willReturn(totalAmount);

		// 3. Mock Behavior Setup (Factory)
		given(reservationFactory.create(mockUser, mockSchedule, List.of(mockSeat), totalAmount))
			.willReturn(mockReservation);

		// 로그에서 사용됨 (User, Schedule) -> 호출되지만, 안전하게 lenient 처리해도 무방
		lenient().when(mockUser.getId()).thenReturn(userId);
		lenient().when(mockSchedule.getId()).thenReturn(scheduleId);

		// DTO 변환 과정에서 사용되지 않을 수 있는 항목들
		lenient().when(mockSeat.getId()).thenReturn(seatId);
		lenient().when(mockReservation.getId()).thenReturn(UUID.randomUUID());
		lenient().when(mockReservation.getTotalPrice()).thenReturn(totalAmount);

		// DTO 내부 객체 Stubbing
		lenient().when(mockSchedule.getPerformanceDatetime()).thenReturn(LocalDateTime.now());
		lenient().when(mockSeat.getStadiumSection()).thenReturn(mockSection);

		// when
		ReservationResponse response = reservationExecutor.reserve(
			userId, scheduleId, performanceId, List.of(seatId), totalAmount
		);

		// then
		assertThat(response).isNotNull();
		assertThat(response.totalAmount()).isEqualTo(totalAmount);

		// Verify Order
		InOrder inOrder = inOrder(
			userRepository, scheduleRepository, seatDefinitionRepository, seatStateRepository,
			priceCalculator, mockSchedule, mockPerformance, reservationRepository
		);

		// 1. 조회
		inOrder.verify(userRepository).findById(userId);
		inOrder.verify(scheduleRepository).findWithPerformanceById(scheduleId);
		inOrder.verify(seatDefinitionRepository).findWithStadiumSectionByIdIn(anyList());
		inOrder.verify(seatStateRepository).findByScheduleIdAndSeatIds(any(), anyList());

		// 2. 도메인 로직
		inOrder.verify(mockSchedule).lockSeatsForReservation(anyList(), eq(1));
		inOrder.verify(priceCalculator).calculateTotalPrice(mockSchedule, List.of(mockSeat));

		// 3. 추가 검증 및 저장
		inOrder.verify(mockPerformance).verifyAgeLimit(mockUser);
		inOrder.verify(reservationRepository).save(mockReservation);
	}

	@Test
	@DisplayName("사용자가 존재하지 않으면 USER_NOT_FOUND 예외가 발생한다")
	void reserve_fail_user_not_found() {
		// given
		UUID userId = UUID.randomUUID();
		given(userRepository.findById(userId)).willReturn(Optional.empty());

		// when and then
		assertThatThrownBy(() -> reservationExecutor.reserve(userId, UUID.randomUUID(), UUID.randomUUID(), List.of(), 1000))
			.isInstanceOf(DomainException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.USER_NOT_FOUND);

		// 검증: 그 이후 로직은 실행되지 않아야 함
		verifyNoInteractions(scheduleRepository, seatDefinitionRepository, reservationRepository);
	}

	@Test
	@DisplayName("스케줄의 공연 정보와 요청한 공연 ID가 다르면 PERFORMANCE_SCHEDULE_MISMATCH 예외가 발생한다")
	void reserve_fail_performance_mismatch() {
		// given
		UUID userId = UUID.randomUUID();
		UUID scheduleId = UUID.randomUUID();
		UUID requestPerformanceId = UUID.randomUUID();
		UUID actualPerformanceId = UUID.randomUUID(); // 불일치 ID

		// 1. User 조회 통과
		given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

		// 2. Schedule 조회
		given(scheduleRepository.findWithPerformanceById(scheduleId)).willReturn(mockSchedule);

		// 3. Performance ID 비교
		given(mockSchedule.getPerformance()).willReturn(mockPerformance);
		given(mockPerformance.getId()).willReturn(actualPerformanceId);

		// when and then

		assertThatThrownBy(() -> reservationExecutor.reserve(userId, scheduleId, requestPerformanceId, List.of(), 1000))
			.isInstanceOf(DomainException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.PERFORMANCE_SCHEDULE_MISMATCH);

		// 검증: 좌석 조회나 저장은 실행되지 않음
		verifyNoInteractions(seatDefinitionRepository, reservationRepository);
	}

	@Test
	@DisplayName("요청한 좌석 수와 조회된 좌석 정의 수가 다르면 SEAT_NOT_DEFINED 예외가 발생한다")
	void reserve_fail_seat_count_mismatch() {
		// given
		UUID userId = UUID.randomUUID();
		UUID scheduleId = UUID.randomUUID();
		UUID performanceId = UUID.randomUUID();
		List<UUID> seatIds = List.of(UUID.randomUUID(), UUID.randomUUID()); // 2개 요청

		// 1~3 단계 통과 설정
		given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
		given(scheduleRepository.findWithPerformanceById(scheduleId)).willReturn(mockSchedule);
		given(mockSchedule.getPerformance()).willReturn(mockPerformance);
		given(mockPerformance.getId()).willReturn(performanceId);

		// 4. 좌석 조회 (DB에는 1개만 있다고 가정 -> 개수 불일치)
		given(seatDefinitionRepository.findWithStadiumSectionByIdIn(seatIds))
			.willReturn(List.of(mockSeat));

		// when and then
		assertThatThrownBy(() -> reservationExecutor.reserve(userId, scheduleId, performanceId, seatIds, 1000))
			.isInstanceOf(DomainException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.SEAT_NOT_DEFINED);

		// 검증: 락, 가격계산, 저장은 실행되지 않음
		verify(mockSchedule, never()).lockSeatsForReservation(anyList(), anyInt());
		verifyNoInteractions(priceCalculator, reservationRepository);
	}

	@Test
	@DisplayName("계산된 가격과 요청 가격이 다르면 TOTAL_PRICE_MISMATCH 예외가 발생한다")
	void reserve_fail_price_mismatch() {
		// given
		int requestAmount = 10000;
		int calculatedAmount = 20000; // 가격 다름
		UUID userId = UUID.randomUUID();
		UUID scheduleId = UUID.randomUUID();
		UUID performanceId = UUID.randomUUID();
		List<UUID> seatIds = List.of(UUID.randomUUID());

		// 1~3 단계 통과
		given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
		given(scheduleRepository.findWithPerformanceById(scheduleId)).willReturn(mockSchedule);
		given(mockSchedule.getPerformance()).willReturn(mockPerformance);
		given(mockPerformance.getId()).willReturn(performanceId);

		// 4. 좌석 개수 일치 통과
		given(seatDefinitionRepository.findWithStadiumSectionByIdIn(seatIds))
			.willReturn(List.of(mockSeat));

		// 5. SeatState 조회 (잠금 로직 수행을 위해 필요)
		given(seatStateRepository.findByScheduleIdAndSeatIds(any(), anyList()))
			.willReturn(List.of(mock(SeatState.class)));

		// (중요) 스케줄 ID는 잠금 로직에서 쓰일 수 있으므로 lenient
		lenient().when(mockSchedule.getId()).thenReturn(scheduleId);

		// 6. 가격 계산 결과 반환 (불일치 유발)
		given(priceCalculator.calculateTotalPrice(mockSchedule, List.of(mockSeat)))
			.willReturn(calculatedAmount);

		// when and then
		assertThatThrownBy(() -> reservationExecutor.reserve(userId, scheduleId, performanceId, seatIds, requestAmount))
			.isInstanceOf(DomainException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.TOTAL_PRICE_MISMATCH);

		// 검증 1: 락은 걸렸어야 함 (가격 계산 전에 락을 거는 로직이므로)
		verify(mockSchedule).lockSeatsForReservation(anyList(), eq(1));

		// 검증 2: 그러나 저장은 절대 되면 안 됨
		verify(reservationRepository, never()).save(any());
		verify(mockPerformance, never()).verifyAgeLimit(any());
	}

	@Test
	@DisplayName("Performance 도메인 객체에서 연령 제한 예외 발생 시 저장하지 않는다")
	void reserve_fail_age_limit() {
		// given
		int amount = 10000;
		UUID userId = UUID.randomUUID();
		UUID scheduleId = UUID.randomUUID();
		UUID performanceId = UUID.randomUUID();
		List<UUID> seatIds = List.of(UUID.randomUUID());

		// 1~3 단계 통과
		given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
		given(scheduleRepository.findWithPerformanceById(scheduleId)).willReturn(mockSchedule);
		given(mockSchedule.getPerformance()).willReturn(mockPerformance);
		given(mockPerformance.getId()).willReturn(performanceId);

		// 4. 좌석 통과
		given(seatDefinitionRepository.findWithStadiumSectionByIdIn(seatIds)).willReturn(List.of(mockSeat));

		// 5. 락 통과 준비
		given(seatStateRepository.findByScheduleIdAndSeatIds(any(), anyList()))
			.willReturn(List.of(mock(SeatState.class)));
		lenient().when(mockSchedule.getId()).thenReturn(scheduleId);

		// 6. 가격 검증 통과
		given(priceCalculator.calculateTotalPrice(any(), anyList())).willReturn(amount);

		// 7. 연령 제한 검증 실패 설정
		doThrow(new DomainException(ErrorCode.ACCESS_DENIED))
			.when(mockPerformance).verifyAgeLimit(mockUser);

		// when and then
		assertThatThrownBy(() -> reservationExecutor.reserve(userId, scheduleId, performanceId, seatIds, amount))
			.isInstanceOf(DomainException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.ACCESS_DENIED);

		// 검증: 락과 가격 계산까지는 호출되었으나, 저장은 안 됨
		verify(mockSchedule).lockSeatsForReservation(anyList(), eq(1));
		verify(priceCalculator).calculateTotalPrice(any(), anyList());
		verify(reservationRepository, never()).save(any());
	}

}