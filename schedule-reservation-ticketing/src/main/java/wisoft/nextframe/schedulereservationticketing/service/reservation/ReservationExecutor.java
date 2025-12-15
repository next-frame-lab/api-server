package wisoft.nextframe.schedulereservationticketing.service.reservation;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.response.ReservationResponse;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.Reservation;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.seat.SeatState;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.repository.reservation.ReservationRepository;
import wisoft.nextframe.schedulereservationticketing.repository.seat.SeatStateRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReservationExecutor {

    private final SeatStateRepository seatStateRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationFactory reservationFactory;

    @Transactional
    public ReservationResponse reserve(ReservationContext context, int totalAmount) {
        final Performance performance = context.performance();
        final User user = context.user();
        final List<SeatDefinition> seats = context.seats();
        final Schedule schedule = context.schedule();

        // 1. 예매 좌석 상태 확인 및 잠금 처리
        final List<UUID> seatIds = seats.stream()
                .map(SeatDefinition::getId)
                .toList();

        final List<SeatState> seatStates = seatStateRepository.findByScheduleIdAndSeatIds(
                schedule.getId(),
                seatIds
        );

        schedule.lockSeatsForReservation(seatStates, seats.size());

        // 2. 예매 정보 생성 및 저장
        final Reservation reservation = reservationFactory.create(user, schedule, seats, totalAmount);
        reservationRepository.save(reservation);
        log.info("Reservation successful for user: {}, schedule: {}", user.getId(), schedule.getId());

        // 3. 응답 DTO 생성 및 반환
        return ReservationResponse.from(reservation, performance, schedule, seats);
    }
}