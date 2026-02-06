package wisoft.nextframe.payment.application.payment.handler;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.payment.application.payment.outbox.cancel.ReservationCancelOutboxService;
import wisoft.nextframe.payment.application.payment.outbox.ticketissue.TicketIssueOutboxService;
import wisoft.nextframe.payment.domain.payment.event.PaymentApprovedEvent;
import wisoft.nextframe.payment.domain.payment.event.PaymentFailedEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventHandler {

	private final TicketIssueOutboxService ticketIssueOutboxService;
	private final ReservationCancelOutboxService reservationCancelOutboxService;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onPaymentApproved(PaymentApprovedEvent event) {
		log.info("결제 승인 이벤트 처리 - paymentId={}, reservationId={}", event.paymentId(), event.reservationId());
		ticketIssueOutboxService.issueOrEnqueue(event.paymentId(), event.reservationId());
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onPaymentFailed(PaymentFailedEvent event) {
		log.info("결제 실패 이벤트 처리 - paymentId={}, reservationId={}", event.paymentId(), event.reservationId());
		reservationCancelOutboxService.cancelOrEnqueue(event.paymentId(), event.reservationId());
	}
}