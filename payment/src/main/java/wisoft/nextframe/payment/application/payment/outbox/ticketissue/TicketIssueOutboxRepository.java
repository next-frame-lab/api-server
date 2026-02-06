package wisoft.nextframe.payment.application.payment.outbox.ticketissue;

import java.time.LocalDateTime;
import java.util.UUID;

import wisoft.nextframe.payment.application.payment.outbox.OutboxRepository;

public interface TicketIssueOutboxRepository extends OutboxRepository<TicketIssueOutboxTarget> {

	void markSuccess(UUID reservationId, UUID ticketId, LocalDateTime now);
}
