package wisoft.nextframe.payment.domain.payment;

import java.time.LocalDateTime;

import wisoft.nextframe.payment.domain.payment.exception.InvalidPaymentStatusException;
import wisoft.nextframe.payment.domain.payment.exception.RefundAlreadyExistsException;
import wisoft.nextframe.payment.domain.refund.Refund;

public class PaymentIssuer {

	public Refund issueRefund(Payment payment, LocalDateTime requestAt, LocalDateTime performanceStartsAt) {
		validatePaymentIsPaid(payment);

		Refund refund = Refund.issue(requestAt, performanceStartsAt, payment.getAmount());
		refund.validateRefundable();

		payment.assignRefund(refund);
		return refund;
	}

	private void validatePaymentIsPaid(Payment payment) {
		if (!payment.isPaid()) {
			throw new InvalidPaymentStatusException("환불", payment.getStatus(), PaymentStatus.PAID);
		}
		if (payment.hasRefunded()) {
			throw new RefundAlreadyExistsException();
		}
	}

}
