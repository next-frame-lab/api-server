package wisoft.nextframe.payment.application.ticketissue.dto;

import java.util.UUID;

public record TicketIssueOutboxTarget(
	UUID reservationId,
	UUID paymentId
) {
}
