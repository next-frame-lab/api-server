package wisoft.nextframe.payment.infra.ticketissue.adapter.dto;

import java.util.UUID;

public record TicketIssueResponse(
	UUID ticketId,
	UUID reservationId,
	String status,
	String issuedAt
) {
}
