package wisoft.nextframe.payment.application.payment.port.output;

import wisoft.nextframe.payment.application.ticketissue.dto.TicketIssueResult;
import wisoft.nextframe.payment.domain.ReservationId;

public interface TicketingClient {
	TicketIssueResult issueTicket(ReservationId reservationId);
}
