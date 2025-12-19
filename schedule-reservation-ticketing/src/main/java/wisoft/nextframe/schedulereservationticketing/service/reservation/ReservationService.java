package wisoft.nextframe.schedulereservationticketing.service.reservation;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

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
@Service
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
    public ReservationResponse reserveSeat(UUID userId, ReservationRequest request) {
        // 1. 예약에 필요한 데이터 준비
        final ReservationContext context = dataProvider.provide(userId, request);

        // 2. 데드락(Deadlock) 방지를 위해 좌석 ID를 정렬하여 락(Lock) 객체 생성
        // 여러 좌석을 동시에 예약할 때, 여러 요청이 서로 다른 순서로 락을 획득하려고 하면 데드락이 발생할 수 있습니다.
        // 예를 들어, 요청 A가 [seat1, seat2] 순서로, 요청 B가 [seat2, seat1] 순서로 락을 획득하려 한다고 가정해봅시다.
        // 요청 A가 seat1 락을 획득하고, 동시에 요청 B가 seat2 락을 획득하면,
        // 서로 상대방이 점유한 락을 무한정 기다리는 '교착 상태(데드락)'에 빠지게 됩니다.
        // .sorted()를 통해 좌석 ID를 항상 일관된 순서(오름차순)로 정렬함으로써,
        // 모든 요청이 같은 순서로 락을 획득하도록 강제하여 데드락을 원천적으로 방지합니다.
        List<RLock> locks = request.seatIds().stream()
            .sorted()
            .map(seatId -> redissonClient.getLock(generateSeatLockKey(request.scheduleId(), seatId)))
            .toList();

        // 3. 여러 개의 락을 하나의 'MultiLock'으로 묶어 원자적(Atomic)으로 관리
        RLock multiLock = new RedissonMultiLock(locks.toArray(new RLock[0]));

        boolean isLockAcquired = false;
        try {
            // 4. 락 획득
            isLockAcquired = multiLock.tryLock(5, 10, TimeUnit.SECONDS);

            if (!isLockAcquired) {
                log.warn("좌석 예매 락 획득 실패. userId: {}, request: {}", userId, request);
                throw new DomainException(ErrorCode.SEAT_ALREADY_LOCKED);
            }
            log.info("좌석 락 획득: {}", request.seatIds());

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
                try {
                    multiLock.unlock();
                    log.info("좌석 락 해제: {}", request.seatIds());
                } catch (Exception e) {
                    log.error("Redis 락 해제 중 예외 발생. seats: {}", request.seatIds(), e);
                }
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