package wisoft.nextframe.payment.application.payment.outbox.ticketissue;

import java.util.UUID;

import wisoft.nextframe.payment.application.payment.outbox.OutboxTarget;

public record TicketIssueOutboxTarget(UUID reservationId, UUID paymentId) implements OutboxTarget {
}
