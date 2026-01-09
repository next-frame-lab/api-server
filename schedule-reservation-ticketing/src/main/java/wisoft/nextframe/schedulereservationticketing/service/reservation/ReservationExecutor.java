package wisoft.nextframe.schedulereservationticketing.service.reservation;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.response.ReservationResponse;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.Reservation;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.seat.SeatState;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.repository.reservation.ReservationRepository;
import wisoft.nextframe.schedulereservationticketing.repository.schedule.ScheduleRepository;
import wisoft.nextframe.schedulereservationticketing.repository.seat.SeatStateRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.SeatDefinitionRepository;
import wisoft.nextframe.schedulereservationticketing.repository.user.UserRepository;

@Slf4j
@RequiredArgsConstructor
@Component
public class ReservationExecutor {

    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final SeatDefinitionRepository seatDefinitionRepository;
    private final SeatStateRepository seatStateRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationFactory reservationFactory;
    private final PriceCalculator priceCalculator;

    @Transactional
    public ReservationResponse reserve(UUID userId,
      UUID scheduleId,
      UUID performanceId,
      List<UUID> seatIds,
      int totalAmount
    ) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
          .orElseThrow(() -> new DomainException(ErrorCode.USER_NOT_FOUND));

        // 2. 스케줄 + 공연 fetch join
        Schedule schedule =
          scheduleRepository.findWithPerformanceById(scheduleId);

        Performance performance = schedule.getPerformance();

        // 3. 공연-스케줄 정합성
        if (!performance.getId().equals(performanceId)) {
            throw new DomainException(ErrorCode.PERFORMANCE_SCHEDULE_MISMATCH);
        }

        // 4. 좌석 정의 조회
        List<SeatDefinition> seats =
          seatDefinitionRepository.findWithStadiumSectionByIdIn(seatIds);

        if (seats.size() != seatIds.size()) {
            throw new DomainException(ErrorCode.SEAT_NOT_DEFINED);
        }

        // 5. 예매 좌석 상태 확인 및 잠금 처리
        final List<SeatState> seatStates = seatStateRepository.findByScheduleIdAndSeatIds(
                schedule.getId(),
                seatIds
        );

        schedule.lockSeatsForReservation(seatStates, seats.size());

        // 6. 가격 검증
        int calculatedPrice = priceCalculator.calculateTotalPrice(schedule, seats);
        if (calculatedPrice != totalAmount) {
            throw new DomainException(ErrorCode.TOTAL_PRICE_MISMATCH);
        }

        // 7. 연령 제한 검증
        performance.verifyAgeLimit(user);

        // 8. 예매 정보 생성 및 저장
        final Reservation reservation = reservationFactory.create(user, schedule, seats, totalAmount);
        reservationRepository.save(reservation);
        log.info("Reservation successful for user: {}, schedule: {}", user.getId(), schedule.getId());

        // 9. 응답 DTO 생성 및 반환
        return ReservationResponse.from(reservation, performance, schedule, seats);
    }
}