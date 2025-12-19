package wisoft.nextframe.schedulereservationticketing.service.reservation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;
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

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

	@InjectMocks
	private ReservationService reservationService;

	@Mock
	private ReservationExecutor reservationExecutor;

	@Mock
	private RedissonClient redissonClient;

	@Mock
	private RLock rLock;

	@Test
	@DisplayName("정상적인 예매 요청 시 락을 획득하고 Executor를 호출한 뒤 락을 해제한다")
	void reserveSeat_success() {
		// given
		UUID userId = UUID.randomUUID();
		UUID scheduleId = UUID.randomUUID();
		UUID performanceId = UUID.randomUUID();
		UUID seatId = UUID.randomUUID();
		int totalAmount = 10000;

		ReservationRequest request = new ReservationRequest(
			performanceId, scheduleId, List.of(seatId), 0L, totalAmount
		);

		// Redisson Mocking
		given(redissonClient.getLock(anyString())).willReturn(rLock);

		// Executor 결과 Mocking
		ReservationResponse expectedResponse = ReservationResponse.builder()
			.reservationId(UUID.randomUUID())
			.totalAmount(totalAmount)
			.build();

		// 변경된 Executor 시그니처에 맞춰 Stubbing
		given(reservationExecutor.reserve(userId, scheduleId, performanceId, List.of(seatId), totalAmount))
			.willReturn(expectedResponse);

		// new RedissonMultiLock() 생성자 가로채기
		try (MockedConstruction<RedissonMultiLock> mockedLock = mockConstruction(RedissonMultiLock.class,
			(mock, context) -> {
				// tryLock 성공 설정
				given(mock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).willReturn(true);
			})) {

			// when
			ReservationResponse response = reservationService.reserveSeat(userId, request);

			// then
			assertThat(response).isEqualTo(expectedResponse);

			// 1. Executor가 올바른 파라미터로 호출되었는지 검증
			verify(reservationExecutor).reserve(userId, scheduleId, performanceId, List.of(seatId), totalAmount);
			// 2. 락 해제(unlock) 호출 확인
			verify(mockedLock.constructed().getFirst()).unlock();
		}
	}

	@Test
	@DisplayName("이미 락이 걸린 좌석을 요청하면 SEAT_ALREADY_LOCKED 예외가 발생한다")
	void reserveSeat_fail_lock_acquisition() {
		// given
		UUID userId = UUID.randomUUID();
		ReservationRequest request = new ReservationRequest(
			UUID.randomUUID(), UUID.randomUUID(), List.of(UUID.randomUUID()), 0L, 10000
		);

		given(redissonClient.getLock(anyString())).willReturn(rLock);

		// tryLock 실패 설정
		try (MockedConstruction<RedissonMultiLock> mockedLock = mockConstruction(RedissonMultiLock.class,
			(mock, context) -> {
				given(mock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).willReturn(false);
			})) {

			// when and then
			assertThatThrownBy(() -> reservationService.reserveSeat(userId, request))
				.isInstanceOf(DomainException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.SEAT_ALREADY_LOCKED);

			// Verify: Executor 실행 안 됨
			verify(reservationExecutor, never()).reserve(any(), any(), any(), anyList(), anyInt());
			// Verify: unlock 호출 안 됨
			verify(mockedLock.constructed().getFirst(), never()).unlock();
		}
	}

	@Test
	@DisplayName("데드락 방지를 위해 좌석 ID 오름차순으로 락을 요청한다")
	void reserveSeat_verify_lock_order() {
		// given
		UUID scheduleId = UUID.randomUUID();
		// ID 순서가 섞여서 들어온다고 가정
		UUID seat1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
		UUID seat2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

		// 요청은 역순(seat2, seat1)으로 들어옴
		ReservationRequest request = new ReservationRequest(
			UUID.randomUUID(), scheduleId, List.of(seat2, seat1), 0L, 10000
		);

		given(redissonClient.getLock(anyString())).willReturn(rLock);

		try (MockedConstruction<RedissonMultiLock> mockedLock = mockConstruction(RedissonMultiLock.class,
			(mock, context) -> given(mock.tryLock(anyLong(), anyLong(), any())).willReturn(true))) {

			// when
			reservationService.reserveSeat(UUID.randomUUID(), request);

			// then
			InOrder inOrder = inOrder(redissonClient);

			// 정렬된 순서(seat1 -> seat2)로 락 요청 확인
			inOrder.verify(redissonClient).getLock(contains(seat1.toString()));
			inOrder.verify(redissonClient).getLock(contains(seat2.toString()));
		}
	}

	@Test
	@DisplayName("Executor 실행 중 예외가 발생하더라도 락은 반드시 해제된다")
	void reserveSeat_fail_business_exception_from_executor() {
		// given
		UUID userId = UUID.randomUUID();
		ReservationRequest request = new ReservationRequest(
			UUID.randomUUID(), UUID.randomUUID(), List.of(UUID.randomUUID()), 0L, 10000
		);

		given(redissonClient.getLock(anyString())).willReturn(rLock);

		// Executor가 예외를 던지도록 설정 (예: 가격 불일치, 이미 예매됨 등 어떤 예외든 상관없음)
		doThrow(new DomainException(ErrorCode.TOTAL_PRICE_MISMATCH))
			.when(reservationExecutor).reserve(any(), any(), any(), anyList(), anyInt());

		try (MockedConstruction<RedissonMultiLock> mockedLock = mockConstruction(RedissonMultiLock.class,
			(mock, ctx) -> given(mock.tryLock(anyLong(), anyLong(), any())).willReturn(true))) {

			// when and then
			assertThatThrownBy(() -> reservationService.reserveSeat(userId, request))
				.isInstanceOf(DomainException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.TOTAL_PRICE_MISMATCH);

			// Verify: 예외가 터져도 unlock은 반드시 수행되어야 함 (finally 블록 검증)
			verify(mockedLock.constructed().getFirst()).unlock();
		}
	}
}