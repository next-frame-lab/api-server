package wisoft.nextframe.schedulereservationticketing.service.reservation;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.request.ReservationRequest;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.response.ReservationResponse;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationService {

    private static final String REDISSON_LOCK_PREFIX = "LOCK:"; // Redis 락 Key 구분자
    private final ReservationExecutor reservationExecutor;
    private final RedissonClient redissonClient;
    private final ReservationDataProvider dataProvider;
    private final PriceCalculator priceCalculator;

    /**
     * 좌석 예매 처리 (분산 락 적용)
     * 동시성 이슈를 방지하기 위해 Redisson 분산 락을 사용하여,
     * 동일한 좌석에 대해 한 번에 하나의 요청만 처리되도록 보장합니다.
     */
    @CacheEvict(cacheNames = "seatStates", key = "#request.scheduleId")
    public ReservationResponse reserveSeat(UUID userId, ReservationRequest request) {
        // 1. 예약에 필요한 데이터 준비
        final ReservationContext context = dataProvider.provide(userId, request);

        // 2. 락 객체 생성 및 데드락(Deadlock) 방지
        List<RLock> locks = request.seatIds().stream()
            .sorted()
            .map(seatId -> redissonClient.getLock(generateSeatLockKey(request.scheduleId(), seatId)))
            .toList();

        // 3. 여러 개의 락을 하나의 'MultiLock'으로 묶어 원자적(Atomic)으로 관리
        RLock multiLock = new RedissonMultiLock(locks.toArray(new RLock[0]));

        boolean isLockAcquired = false;
        try {
            // 4. 락 획득
            isLockAcquired = multiLock.tryLock(5, 3, TimeUnit.SECONDS);

            if (!isLockAcquired) {
                log.warn("좌석 예매 락 획득 실패. userId: {}, request: {}", userId, request);
                throw new DomainException(ErrorCode.SEAT_ALREADY_LOCKED);
            }
            log.info("Get lock success for seats: {}", request.seatIds());

            // 5. 예매 관련 검증
            validateReservation(context, request);

            // 6. 예매 트랜잭션 수행
            return reservationExecutor.reserve(context, request.totalAmount());

        } catch (InterruptedException e) {
            // 스레드 인터럽트(시스템 셧다운, 스레드 강제 종료 등)
            Thread.currentThread().interrupt();
            throw new DomainException(ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            // 7. 락 해제
            if (isLockAcquired) {
                multiLock.unlock();
                log.info("Unlock success for seats: {}", request.seatIds());
            }
        }
    }

    private void validateReservation(ReservationContext context, ReservationRequest request) {
        final Performance performance = context.performance();
        final User user = context.user();
        final List<SeatDefinition> seats = context.seats();
        final Schedule schedule = context.schedule();

        // 1. 사용자 연령 제한 검증(공연 관람 등급 확인)
        performance.verifyAgeLimit(user);

        // 2. 클라이언트가 요청한 금액과 서버에서 계산한 금액이 일치하는지 확인
        final int calculatedTotalPrice = priceCalculator.calculateTotalPrice(schedule, seats);
        if (calculatedTotalPrice != request.totalAmount()) {
            log.warn("요청 금액 불일치. userId: {}, scheduleId: {}, clientAmount: {}, serverAmount: {}",
                    user.getId(), request.scheduleId(), request.totalAmount(), calculatedTotalPrice);
            throw new DomainException(ErrorCode.TOTAL_PRICE_MISMATCH);
        }
    }

    private String generateSeatLockKey(UUID scheduleId, UUID seatId) {
        // Redis Key 패턴: LOCK:sch:{scheduleId}:seat:{seatId}
        return REDISSON_LOCK_PREFIX + "sch:" + scheduleId + ":seat:" + seatId;
    }
}