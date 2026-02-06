package wisoft.nextframe.payment.application.payment.outbox.cancel;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.payment.application.payment.port.output.ReservationCancelClient;
import wisoft.nextframe.payment.domain.ReservationId;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationCancelOutboxService {

	private final ReservationCancelClient reservationCancelClient;
	private final ReservationCancelOutboxRepository outboxRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void cancelOrEnqueue(UUID paymentId, UUID reservationId) {
		log.debug("예약 취소 요청 - paymentId={}, reservationId={}", paymentId, reservationId);

		LocalDateTime now = LocalDateTime.now();

		// 1) 먼저 outbox에 PENDING upsert (쓰기 선반영)
		outboxRepository.upsertPending(paymentId, reservationId, null, now);
		try {
			// 2) 외부 호출 (DELETE /reservations/{reservation-id})
			reservationCancelClient.cancelReservation(ReservationId.of(reservationId));

			// 3) 성공 처리
			outboxRepository.markSuccess(reservationId, now);
			log.info("예약 취소 성공 - reservationId={}", reservationId);
		} catch (Exception e) {
			// 4) 실패면 lastError만 업데이트 (이미 PENDING row는 있음)
			log.warn("예약 취소 실패, 재시도 대기열에 추가 - reservationId={}, error={}",
				reservationId, e.getMessage());
			outboxRepository.upsertPending(paymentId, reservationId, e.toString(), now);
		}
	}
}
