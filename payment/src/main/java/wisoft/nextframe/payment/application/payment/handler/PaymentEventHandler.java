package wisoft.nextframe.payment.application.payment.handler;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.payment.application.ticketissue.TicketIssueOutboxService;
import wisoft.nextframe.payment.domain.payment.event.PaymentApprovedEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventHandler {

	private final TicketIssueOutboxService outboxService;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onPaymentApproved(PaymentApprovedEvent event) {
		log.info("HANDLER 호출 paymentId={}, reservationId={}", event.paymentId(), event.reservationId());

		outboxService.issueOrEnqueue(event.paymentId(), event.reservationId());
	}
}