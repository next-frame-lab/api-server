package wisoft.nextframe.payment.application.payment.outbox.cancel;

import java.time.LocalDateTime;
import java.util.UUID;

import wisoft.nextframe.payment.application.payment.outbox.OutboxRepository;

public interface ReservationCancelOutboxRepository extends OutboxRepository<ReservationCancelOutboxTarget> {

	void markSuccess(UUID reservationId, LocalDateTime now);
}
