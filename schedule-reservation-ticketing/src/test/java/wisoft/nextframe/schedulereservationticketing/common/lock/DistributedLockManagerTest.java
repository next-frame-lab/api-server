package wisoft.nextframe.schedulereservationticketing.common.lock;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class DistributedLockManagerTest {

	@Mock
	private RedissonClient redissonClient;

	@Mock
	private RLock individualLock;

	@Mock
	private RLock multiLock;

	@InjectMocks
	@Spy
	private DistributedLockManager distributedLockManager;

	@Test
	@DisplayName("락 획득 성공 시 비즈니스 로직을 실행하고 락을 해제한다")
	void executeWithLock_success() throws InterruptedException {
		// given
		List<String> keys = List.of("seat:1", "seat:2");
		String expectedResult = "Business Logic Result";

		// 1. RedissonClient가 개별 락 Mock을 반환하도록 설정
		given(redissonClient.getLock(anyString())).willReturn(individualLock);

		// 2. createMultiLock 호출 시 우리가 만든 multiLock Mock을 반환하도록 설정
		// Spy 객체에는 when(...).thenReturn(...) 대신 doReturn(...).when(...)을 사용하는 것이 안전
		doReturn(multiLock).when(distributedLockManager).createMultiLock(anyList());

		// 3. 락 획득 성공 시나리오 (true 반환)
		given(multiLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).willReturn(true);

		// when
		String result = distributedLockManager.executeWithLock(keys, () -> expectedResult);

		// then
		assertThat(result).isEqualTo(expectedResult);

		// 검증: 락 획득 시도 -> 락 해제 순서 및 호출 여부 확인
		verify(multiLock).tryLock(anyLong(), anyLong(), any(TimeUnit.class));
		verify(multiLock).unlock();
	}

	@Test
	@DisplayName("락 획득 실패 시 DomainException(SEAT_ALREADY_LOCKED)을 던진다")
	void executeWithLock_fail_acquisition() throws InterruptedException {
		// given
		List<String> keys = List.of("seat:1");

		given(redissonClient.getLock(anyString())).willReturn(individualLock);
		doReturn(multiLock).when(distributedLockManager).createMultiLock(anyList());

		// 락 획득 실패 시나리오 (false 반환)
		given(multiLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).willReturn(false);

		// when and then
		assertThatThrownBy(() -> distributedLockManager.executeWithLock(keys, () -> "Success"))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEAT_ALREADY_LOCKED); // ErrorCode 필드 검증

		// 검증: 락 획득에 실패했으므로 unlock은 절대 호출되면 안 됨
		verify(multiLock, never()).unlock();
	}

	@Test
	@DisplayName("락 획득 중 InterruptedException 발생 시 DomainException(INTERNAL_SERVER_ERROR)을 던진다")
	void executeWithLock_interrupted() throws InterruptedException {
		// given
		List<String> keys = List.of("seat:1");

		given(redissonClient.getLock(anyString())).willReturn(individualLock);
		doReturn(multiLock).when(distributedLockManager).createMultiLock(anyList());

		// InterruptedException 발생 시나리오
		given(multiLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class)))
			.willThrow(new InterruptedException("Thread interrupted"));

		// when and then
		assertThatThrownBy(() -> distributedLockManager.executeWithLock(keys, () -> "Success"))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INTERNAL_SERVER_ERROR);

		// 현재 스레드의 인터럽트 상태가 복구되었는지 확인
		assertThat(Thread.currentThread().isInterrupted()).isTrue();

		// 테스트를 위해 인터럽트 상태 초기화
		Thread.interrupted();

		// 검증: 예외 발생 시 finally 블록으로 가지만 isLockAcquired가 false이므로 unlock 호출 안 됨
		verify(multiLock, never()).unlock();
	}

	@Test
	@DisplayName("락 해제(unlock) 중 예외가 발생해도 비즈니스 로직 결과는 정상 반환된다")
	void executeWithLock_unlock_exception() throws InterruptedException {
		// given
		List<String> keys = List.of("seat:1");
		String expectedResult = "Success";

		given(redissonClient.getLock(anyString())).willReturn(individualLock);
		doReturn(multiLock).when(distributedLockManager).createMultiLock(anyList());
		given(multiLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).willReturn(true);

		// 락 해제 시 예외 발생 (로그만 찍고 로직은 정상 종료되어야 함)
		doThrow(new RuntimeException("Unlock Failed")).when(multiLock).unlock();

		// when
		String result = distributedLockManager.executeWithLock(keys, () -> expectedResult);

		// then
		assertThat(result).isEqualTo(expectedResult);

		// unlock이 호출되었는지 확인
		verify(multiLock).unlock();
	}
}