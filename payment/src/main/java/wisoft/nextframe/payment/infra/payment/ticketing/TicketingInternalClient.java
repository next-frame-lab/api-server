package wisoft.nextframe.payment.infra.payment.ticketing;

import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.payment.application.payment.port.output.TicketingClient;
import wisoft.nextframe.schedulereservationticketing.ticketing.service.TicketService;

@Component
@RequiredArgsConstructor
public class TicketingInternalClient implements TicketingClient {

	private final TicketService ticketService;

	@Override
	public void issueTicket(UUID reservationId) {
		ticketService.issueByReservation(reservationId);
	}
}
