package wisoft.nextframe.ticketing.domain;

import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.ticketing.domain.exception.CannotIssueTicketWithoutPaymentException;

public class TicketService {
	public Ticket issue(Payment payment) {
		if (payment == null) {
			throw new IllegalArgumentException("Payment cannot be null");
		}

		if (!payment.isPaid()) {
			throw new CannotIssueTicketWithoutPaymentException();
		}

		return Ticket.issueFrom(payment); // QR 생성, 유효성 등 내부 처리
	}
}
