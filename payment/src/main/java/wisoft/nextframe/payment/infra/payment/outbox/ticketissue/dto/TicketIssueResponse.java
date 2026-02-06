package wisoft.nextframe.payment.infra.payment.outbox.ticketissue.dto;

import java.util.UUID;

public record TicketIssueResponse(
	UUID ticketId,
	UUID reservationId,
	String status,
	String issuedAt
) {
}
