package wisoft.nextframe.payment.application.payment.port.output;

import wisoft.nextframe.payment.application.payment.outbox.ticketissue.TicketIssueResult;
import wisoft.nextframe.payment.domain.ReservationId;

public interface TicketingClient {
	TicketIssueResult issueTicket(ReservationId reservationId);
}
