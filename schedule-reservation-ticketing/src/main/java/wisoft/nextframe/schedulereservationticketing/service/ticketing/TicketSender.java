package wisoft.nextframe.schedulereservationticketing.service.ticketing;

import wisoft.nextframe.schedulereservationticketing.dto.ticketing.TicketInfoResponse;

public interface TicketSender {
	void send(TicketInfoResponse ticket, String recipientEmail);
}
