package wisoft.nextframe.payment.application.payment.port.output;

import wisoft.nextframe.payment.domain.ReservationId;
import wisoft.nextframe.payment.infra.ticketing.adapter.dto.TicketIssueResponse;

public interface TicketingClient {
	TicketIssueResponse issueTicket(ReservationId reservationId);
}
