package wisoft.nextframe.payment.domain.payment.event;

import java.util.UUID;

public record PaymentFailedEvent(UUID paymentId, UUID reservationId) implements DomainEvent {

}