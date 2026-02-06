package wisoft.nextframe.payment.application.payment.outbox.ticketissue;

import java.util.UUID;

public record TicketIssueResult(
	UUID ticketId
) {
}
