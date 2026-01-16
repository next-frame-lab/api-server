package wisoft.nextframe.payment.application.ticketissue.port.output;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import wisoft.nextframe.payment.application.ticketissue.dto.TicketIssueOutboxRow;

public interface TicketIssueOutboxRepository {

	void upsertPending(UUID paymentId, UUID reservationId, String lastError, LocalDateTime now);

	void markSuccess(UUID reservationId, UUID ticketId, LocalDateTime now);

	List<TicketIssueOutboxRow> findReady(LocalDateTime now, int limit);

	void failAndBackoff(UUID reservationId, String lastError, LocalDateTime now);

	long countPending();
}
