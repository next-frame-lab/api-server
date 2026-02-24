package wisoft.nextframe.schedulereservationticketing.service.reservation;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.common.lock.DistributedLockManager;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.request.ReservationRequest;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.response.ReservationResponse;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.Reservation;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.ReservationSeat;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.ReservationStatus;
import wisoft.nextframe.schedulereservationticketing.entity.seat.SeatState;
import wisoft.nextframe.schedulereservationticketing.repository.reservation.ReservationRepository;
import wisoft.nextframe.schedulereservationticketing.repository.seat.SeatStateRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

	private final ReservationExecutor reservationExecutor;
	private final DistributedLockManager distributedLockManager;
	private final ReservationRepository reservationRepository;
	private final SeatStateRepository seatStateRepository;

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
		return distributedLockManager.executeWithLock(lockKeys, () ->
			reservationExecutor.reserve(
				userId,
				request.scheduleId(),
				request.performanceId(),
				request.seatIds(),
				request.totalAmount()
			)
		);
	}

	/**
	 * 예약 취소 + 좌석 해제 (결제 실패 / 환불 시 payment 서버에서 호출)
	 * - CREATED: 결제 실패로 인한 취소
	 * - CONFIRMED: 환불로 인한 취소
	 * - CANCELLED: 멱등 처리 (이미 취소된 경우 그대로 반환)
	 */
	@Transactional
	public void cancelReservation(UUID reservationId) {
		Reservation reservation = reservationRepository.findByIdWithSeats(reservationId)
			.orElseThrow(() -> new DomainException(ErrorCode.RESERVATION_NOT_FOUND));

		if (reservation.getStatus() == ReservationStatus.CANCELLED) {
			log.info("이미 취소된 예약 - reservationId={}", reservationId);
			return;
		}

		if (reservation.getStatus() == ReservationStatus.CREATED) {
			reservation.cancel();
		} else {
			reservation.cancelForRefund();
		}

		List<ReservationSeat> reservationSeats = reservation.getReservationSeats();
		if (!reservationSeats.isEmpty()) {
			UUID scheduleId = reservation.getSchedule().getId();
			List<UUID> seatIds = reservationSeats.stream()
				.map(rs -> rs.getId().getSeatId())
				.toList();
			List<SeatState> seatStates = seatStateRepository.findByScheduleIdAndSeatIds(scheduleId, seatIds);
			seatStates.forEach(SeatState::unlock);
		}

		log.info("예약 취소 완료 - reservationId={}, 이전 상태={}", reservationId, reservation.getStatus());
	}

	private String generateSeatLockKey(UUID scheduleId, UUID seatId) {
		return "lock:sch:" + scheduleId + ":seat:" + seatId;
	}
}
