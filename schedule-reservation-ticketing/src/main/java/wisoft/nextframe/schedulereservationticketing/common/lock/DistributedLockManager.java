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

    public <T> T executeWithLock(List<String> lockKeys, Supplier<T> businessLogic) {
        List<RLock> locks = lockKeys.stream()
            .map(redissonClient::getLock)
            .toList();

        RLock multiLock = createMultiLock(locks);

        boolean isLockAcquired = false;
        try {
            isLockAcquired = multiLock.tryLock(WAIT_TIME_SECONDS, LEASE_TIME_SECONDS, TimeUnit.SECONDS);

            if (!isLockAcquired) {
                log.warn("Failed to acquire lock for keys: {}", lockKeys);
                throw new DomainException(ErrorCode.SEAT_ALREADY_LOCKED);
            }
            log.info("Acquired lock for keys: {}", lockKeys);

            return businessLogic.get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DomainException(ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            if (isLockAcquired) {
                try {
                    multiLock.unlock();
                    log.info("Released lock for keys: {}", lockKeys);
                } catch (Exception e) {
                    log.error("An error occurred while releasing the lock. Keys: {}", lockKeys, e);
                }
            }
        }
    }

    protected RLock createMultiLock(List<RLock> locks) {
        return new RedissonMultiLock(locks.toArray(new RLock[0]));
    }
}
