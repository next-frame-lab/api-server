package wisoft.nextframe.payment.application.payment.port.output;

import java.util.UUID;

public record TicketIssueResult(
	UUID ticketId
) {
}
