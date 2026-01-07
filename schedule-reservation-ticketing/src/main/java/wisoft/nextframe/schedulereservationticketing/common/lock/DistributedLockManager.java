package wisoft.nextframe.schedulereservationticketing.common.lock;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;

@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockManager {

    private final RedissonClient redissonClient;
    private static final long WAIT_TIME_SECONDS = 2;
    private static final long LEASE_TIME_SECONDS = 3;

    /**
     * 여러 개의 Key에 대해 락을 획득한 후 비즈니스 로직 수행
     * 모든 락을 획득하거나, 하나도 획득하지 못하는 원자성 보장
     *
     * @param lockKeys 락을 걸 대상 키 목록 (예매할 좌석 ID 리스트)
     * @param businessLogic 락 획득 성공 시 실행할 로직 (Supplier)
     * @return 비즈니스 로직의 반환값
     */
    public <T> T executeWithLock(List<String> lockKeys, Supplier<T> businessLogic) {
        List<RLock> locks = lockKeys.stream()
            .map(redissonClient::getLock)
            .toList();

        RLock multiLock = createMultiLock(locks);

        boolean isLockAcquired = false;
        try {
            // 락 획득 시도
            isLockAcquired = multiLock.tryLock(WAIT_TIME_SECONDS, LEASE_TIME_SECONDS, TimeUnit.SECONDS);

            // 락 획득 실패 시
            if (!isLockAcquired) {
                log.warn("락 획득 실패 (대상 키: {})", lockKeys);
                throw new DomainException(ErrorCode.SEAT_ALREADY_LOCKED);
            }
            log.info("락 획득 성공 (대상 키: {})", lockKeys);

            return businessLogic.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DomainException(ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            if (isLockAcquired) {
                try {
                    multiLock.unlock();
                    log.info("락 해제 완료 (대상 키: {})", lockKeys);
                } catch (Exception e) {
                    log.error("락 해제 중 오류 발생 (대상 키: {})", lockKeys, e);
                }
            }
        }
    }

    /**
     * MultiLock 생성 메서드
     * protected로 선언하여 테스트 코드에서 Mocking 하거나 오버라이딩 할 수 있도록 설계
     */
    protected RLock createMultiLock(List<RLock> locks) {
        return new RedissonMultiLock(locks.toArray(new RLock[0]));
    }
}
