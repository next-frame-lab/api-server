package wisoft.nextframe.payment.application.payment.port.output;

import wisoft.nextframe.payment.domain.ReservationId;

public interface TicketingClient {
	void issueTicket(ReservationId reservationId);
}
