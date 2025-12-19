package wisoft.nextframe.schedulereservationticketing.service.reservation;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.request.ReservationRequest;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.response.ReservationResponse;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

	@InjectMocks
	private ReservationService reservationService;

	@Mock
	private ReservationExecutor reservationExecutor;

	@Mock
	private RedissonClient redissonClient;

	@Mock
	private ReservationDataProvider dataProvider;

	@Mock
	private PriceCalculator priceCalculator;

	@Mock
	private RLock rLock; // 개별 좌석 락 Mock

	@Test
	@DisplayName("정상적인 예매 요청 시 예매가 성공하고 락이 해제된다")
	void reserveSeat_success() {
		// given
		UUID userId = UUID.randomUUID();
		UUID scheduleId = UUID.randomUUID();
		UUID seatId = UUID.randomUUID();
		int totalAmount = 10000;

		ReservationRequest request = new ReservationRequest(
			UUID.randomUUID(), scheduleId, List.of(seatId), 0L, totalAmount
		);

		// Context Mocking
		Performance mockPerformance = mock(Performance.class); // 내부 로직 격리를 위해 Mock 사용
		ReservationContext context = new ReservationContext(
			mock(User.class), mock(Schedule.class), mockPerformance, List.of(mock(SeatDefinition.class))
		);

		given(dataProvider.provide(userId, request)).willReturn(context);
		given(priceCalculator.calculateTotalPrice(any(), any())).willReturn(totalAmount);

		// Redisson Mocking
		given(redissonClient.getLock(anyString())).willReturn(rLock);

		// 결과 Mocking
		ReservationResponse expectedResponse = ReservationResponse.builder()
			.reservationId(UUID.randomUUID())
			.totalAmount(totalAmount)
			.build();
		given(reservationExecutor.reserve(context, totalAmount)).willReturn(expectedResponse);

		// new RedissonMultiLock() 생성자 가로채기
		try (MockedConstruction<RedissonMultiLock> mockedLock = mockConstruction(RedissonMultiLock.class,
			(mock, context1) -> {
				// tryLock이 true를 반환하도록 설정
				given(mock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).willReturn(true);
			})) {

			// when
			ReservationResponse response = reservationService.reserveSeat(userId, request);

			// then
			assertThat(response).isEqualTo(expectedResponse);

			// 1. 비즈니스 검증 통과 확인
			verify(mockPerformance).verifyAgeLimit(any());
			// 2. 예매 실행 확인
			verify(reservationExecutor).reserve(context, totalAmount);
			// 3. 락 해제(unlock) 호출 확인 (mockedLock.constructed().get(0)은 생성된 첫 번째 Mock 객체)
			verify(mockedLock.constructed().getFirst()).unlock();
		}
	}

	@Test
	@DisplayName("이미 락이 걸린 좌석을 요청하면 SEAT_ALREADY_LOCKED 예외가 발생한다")
	void reserveSeat_fail_lock_acquisition() {
		// given
		UUID userId = UUID.randomUUID();
		UUID seatId = UUID.randomUUID();
		ReservationRequest request = new ReservationRequest(
			UUID.randomUUID(), UUID.randomUUID(), List.of(seatId), 0L, 10000
		);

		given(dataProvider.provide(any(), any())).willReturn(mock(ReservationContext.class));
		given(redissonClient.getLock(anyString())).willReturn(rLock);

		// 생성자 가로채기: tryLock 실패 설정
		try (MockedConstruction<RedissonMultiLock> mockedLock = mockConstruction(RedissonMultiLock.class,
			(mock, context) -> {
				given(mock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).willReturn(false);
			})) {

			// when and then
			assertThatThrownBy(() -> reservationService.reserveSeat(userId, request))
				.isInstanceOf(DomainException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.SEAT_ALREADY_LOCKED);

			// Verify: 예매 로직은 실행되지 않아야 함
			verify(reservationExecutor, never()).reserve(any(), anyInt());
			// Verify: 획득 실패했으므로 unlock도 호출되지 않아야 함
			verify(mockedLock.constructed().getFirst(), never()).unlock();
		}
	}

	@Test
	@DisplayName("데드락 방지를 위해 좌석 ID 오름차순으로 락을 요청한다")
	void reserveSeat_verify_lock_order() {
		// given
		UUID scheduleId = UUID.randomUUID();
		// ID 순서가 섞여서 들어온다고 가정 (UUID 문자열 비교로 정렬됨)
		// UUID 정렬 순서를 확실히 하기 위해 문자열로 생성
		UUID seat1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
		UUID seat2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

		// 요청은 역순(seat2, seat1)으로 들어옴
		ReservationRequest request = new ReservationRequest(
			UUID.randomUUID(), scheduleId, List.of(seat2, seat1), 0L, 10000
		);

		Performance mockPerformance = mock(Performance.class);
		User mockUser = mock(User.class);
		Schedule mockSchedule = mock(Schedule.class);

		ReservationContext context = new ReservationContext(
			mockUser, mockSchedule, mockPerformance, List.of(mock(SeatDefinition.class))
		);

		given(dataProvider.provide(any(), any())).willReturn(context);
		given(redissonClient.getLock(anyString())).willReturn(rLock);

		// 가격 검증 통과를 위해 Stubbing (안하면 가격 불일치 예외 발생)
		given(priceCalculator.calculateTotalPrice(any(), any())).willReturn(10000);

		try (MockedConstruction<RedissonMultiLock> mockedLock = mockConstruction(RedissonMultiLock.class,
			(mock, cntx) -> given(mock.tryLock(anyLong(), anyLong(), any())).willReturn(true))) {

			// when
			reservationService.reserveSeat(UUID.randomUUID(), request);

			// then
			InOrder inOrder = inOrder(redissonClient);

			// seat1이 먼저 요청되고 seat2가 나중에 요청되어야 함
			inOrder.verify(redissonClient).getLock(contains(seat1.toString()));
			inOrder.verify(redissonClient).getLock(contains(seat2.toString()));
		}
	}

	@Test
	@DisplayName("요청 금액과 서버 계산 금액이 다르면 예외가 발생하고 락은 해제된다")
	void reserveSeat_fail_price_mismatch() {
		// given
		int clientAmount = 10000;
		int serverAmount = 15000; // 금액 불일치
		ReservationRequest request = new ReservationRequest(
			UUID.randomUUID(), UUID.randomUUID(), List.of(UUID.randomUUID()), 0L, clientAmount
		);

		ReservationContext context = mock(ReservationContext.class);
		Performance performance = mock(Performance.class);

		given(context.performance()).willReturn(performance);
		given(context.user()).willReturn(mock(User.class));
		given(dataProvider.provide(any(), any())).willReturn(context);

		given(redissonClient.getLock(anyString())).willReturn(rLock);
		given(priceCalculator.calculateTotalPrice(any(), any())).willReturn(serverAmount); // 서버 금액 리턴

		try (MockedConstruction<RedissonMultiLock> mockedLock = mockConstruction(RedissonMultiLock.class,
			(mock, ctx) -> given(mock.tryLock(anyLong(), anyLong(), any())).willReturn(true))) {

			// when and then
			assertThatThrownBy(() -> reservationService.reserveSeat(UUID.randomUUID(), request))
				.isInstanceOf(DomainException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.TOTAL_PRICE_MISMATCH);

			// 락 해제는 반드시 수행되어야 함
			verify(mockedLock.constructed().getFirst()).unlock();
			// 예매 실행 안됨
			verify(reservationExecutor, never()).reserve(any(), anyInt());
		}
	}

	@Test
	@DisplayName("연령 제한에 걸리면 ACCESS_DENIED 예외가 발생하고 락은 해제된다")
	void reserveSeat_fail_age_limit() {
		// given
		ReservationRequest request = new ReservationRequest(
			UUID.randomUUID(), UUID.randomUUID(), List.of(UUID.randomUUID()), 0L, 10000
		);

		ReservationContext context = mock(ReservationContext.class);
		Performance performance = mock(Performance.class);
		User user = mock(User.class); // 간단히 Mock 처리

		given(context.performance()).willReturn(performance);
		given(context.user()).willReturn(user);
		given(dataProvider.provide(any(), any())).willReturn(context);

		given(redissonClient.getLock(anyString())).willReturn(rLock);

		// Performance.verifyAgeLimit 호출 시 예외 발생 설정
		doThrow(new DomainException(ErrorCode.ACCESS_DENIED))
			.when(performance).verifyAgeLimit(user);

		try (MockedConstruction<RedissonMultiLock> mockedLock = mockConstruction(RedissonMultiLock.class,
			(mock, ctx) -> given(mock.tryLock(anyLong(), anyLong(), any())).willReturn(true))) {

			// when and then
			assertThatThrownBy(() -> reservationService.reserveSeat(UUID.randomUUID(), request))
				.isInstanceOf(DomainException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.ACCESS_DENIED);

			// Verify: 락 해제는 반드시 수행되어야 함 (finally 블록)
			verify(mockedLock.constructed().getFirst()).unlock();
		}
	}
}