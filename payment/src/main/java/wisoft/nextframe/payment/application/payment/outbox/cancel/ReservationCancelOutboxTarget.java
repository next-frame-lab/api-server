package wisoft.nextframe.payment.application.payment.outbox.cancel;

import java.util.UUID;

import wisoft.nextframe.payment.application.payment.outbox.OutboxTarget;

public record ReservationCancelOutboxTarget(UUID reservationId, UUID paymentId) implements OutboxTarget {
}
