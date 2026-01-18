package wisoft.nextframe.payment.application.payment.handler;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.payment.application.payment.port.output.TicketingClient;
import wisoft.nextframe.payment.domain.ReservationId;
import wisoft.nextframe.payment.domain.payment.event.PaymentApprovedEvent;

@Component
@RequiredArgsConstructor
public class PaymentEventHandler {

	private final TicketingClient ticketingClient;

	@EventListener
	public void onPaymentApproved(PaymentApprovedEvent event) {
		// 결제가 승인되면 티켓 발급 요청
		ticketingClient.issueTicket(ReservationId.of(event.reservationId()));
	}
}
