package wisoft.nextframe.payment.application.payment.outbox.cancel;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.payment.application.payment.outbox.AbstractOutboxRetryUseCase;
import wisoft.nextframe.payment.application.payment.outbox.OutboxRepository;
import wisoft.nextframe.payment.application.payment.port.output.ReservationCancelClient;
import wisoft.nextframe.payment.domain.ReservationId;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationCancelRetryUseCase extends AbstractOutboxRetryUseCase<ReservationCancelOutboxTarget> {

	private final ReservationCancelOutboxRepository outboxRepository;
	private final ReservationCancelClient reservationCancelClient;

	@Override
	protected OutboxRepository<ReservationCancelOutboxTarget> getRepository() {
		return outboxRepository;
	}

	@Override
	protected void executeExternalCall(ReservationCancelOutboxTarget target, LocalDateTime now) {
		reservationCancelClient.cancelReservation(ReservationId.of(target.reservationId()));
		outboxRepository.markSuccess(target.reservationId(), now);
		log.info("예약 취소 재시도 성공 - reservationId={}", target.reservationId());
	}

	@Override
	protected String getLogPrefix() {
		return "reservation_cancel_retry";
	}
}
