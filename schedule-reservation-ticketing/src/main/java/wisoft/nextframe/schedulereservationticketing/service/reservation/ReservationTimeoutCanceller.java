package wisoft.nextframe.schedulereservationticketing.service.reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.Reservation;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.ReservationSeat;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.ReservationStatus;
import wisoft.nextframe.schedulereservationticketing.entity.seat.SeatState;
import wisoft.nextframe.schedulereservationticketing.repository.reservation.ReservationRepository;
import wisoft.nextframe.schedulereservationticketing.repository.seat.SeatStateRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationTimeoutCanceller {

	private final ReservationRepository reservationRepository;
	private final SeatStateRepository seatStateRepository;
	private final TransactionTemplate transactionTemplate;

	public int cancelExpiredReservations(LocalDateTime now) {
		final List<Reservation> expiredReservations = transactionTemplate.execute(status ->
			reservationRepository.findExpiredReservations(ReservationStatus.CREATED, now)
		);

		if (expiredReservations == null || expiredReservations.isEmpty()) {
			return 0;
		}

		log.info("만료된 예약 {}건 처리 시작", expiredReservations.size());

		int cancelledCount = 0;
		for (final Reservation reservation : expiredReservations) {
			try {
				transactionTemplate.executeWithoutResult(status ->
					cancelReservationAndUnlockSeats(reservation)
				);
				cancelledCount++;
			} catch (ObjectOptimisticLockingFailureException e) {
				log.info("예약 {}이 동시에 처리됨 (결제 확정), 건너뜀", reservation.getId());
			}
		}

		log.info("만료된 예약 {}건 처리 완료", cancelledCount);
		return cancelledCount;
	}

	private void cancelReservationAndUnlockSeats(Reservation reservation) {
		reservation.cancel();

		final List<ReservationSeat> reservationSeats = reservation.getReservationSeats();

		if (reservationSeats.isEmpty()) {
			log.warn("예약 {}에 연결된 좌석이 없습니다.", reservation.getId());
			return;
		}

		final UUID scheduleId = reservation.getSchedule().getId();
		final List<UUID> seatIds = reservationSeats.stream()
			.map(rs -> rs.getId().getSeatId())
			.toList();

		final List<SeatState> seatStates = seatStateRepository
			.findByScheduleIdAndSeatIds(scheduleId, seatIds);

		for (final SeatState seatState : seatStates) {
			seatState.unlock();
		}

		log.info("예약 {} 취소 완료 (좌석 {}개 잠금 해제)", reservation.getId(), seatStates.size());
	}
}
