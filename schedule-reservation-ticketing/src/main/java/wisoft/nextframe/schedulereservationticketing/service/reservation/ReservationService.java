package wisoft.nextframe.schedulereservationticketing.service.reservation;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.common.lock.DistributedLockManager;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.request.ReservationRequest;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.response.ReservationResponse;
import wisoft.nextframe.schedulereservationticketing.service.seat.SeatStateService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationExecutor reservationExecutor;
    private final DistributedLockManager distributedLockManager;
    private final SeatStateService seatStateService;

    /**
     * 좌석 예매 처리 (분산 락 적용)
     * 동시성 이슈를 방지하기 위해 분산 락을 사용하여,
     * 동일한 좌석에 대해 한 번에 하나의 요청만 처리되도록 보장합니다.
     * DB 트랜잭션 커밋과 캐시 무효화의 원자성을 보장하기 위해 @Transactional로 관리됩니다.
     */
    public ReservationResponse reserveSeat(UUID userId, ReservationRequest request) {
        // 1. 데드락 방지를 위해 좌석 ID를 정렬하여 락 키 생성
        List<String> lockKeys = request.seatIds().stream()
            .sorted()
            .map(seatId -> generateSeatLockKey(request.scheduleId(), seatId))
            .collect(Collectors.toList());

        // 2. 분산 락을 사용하여 예매 트랜잭션 수행
        ReservationResponse response = distributedLockManager.executeWithLock(lockKeys, () ->
            reservationExecutor.reserve(
                userId,
                request.scheduleId(),
                request.performanceId(),
                request.seatIds(),
                request.totalAmount()
            )
        );

        // 3. 예매 완료 후 좌석 상태 캐시 무효화
        // @Transactional 어노테이션에 의해, 이 캐시 무효화 작업은
        // reserveSeat 메서드의 트랜잭션이 성공적으로 커밋된 후에 실행됩니다.
        seatStateService.evictSeatStatesCache(request.scheduleId());

        return response;
    }

    private String generateSeatLockKey(UUID scheduleId, UUID seatId) {
        return "lock:sch:" + scheduleId + ":seat:" + seatId;
    }
}
