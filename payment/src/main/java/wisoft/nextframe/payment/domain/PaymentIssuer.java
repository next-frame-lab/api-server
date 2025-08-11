package wisoft.nextframe.payment.domain;

import java.time.LocalDateTime;

import wisoft.nextframe.payment.domain.exception.InvalidPaymentStatusException;
import wisoft.nextframe.payment.domain.exception.RefundAlreadyExistsException;
import wisoft.nextframe.payment.refund.Refund;

public class PaymentIssuer {

	public Refund issueRefund(Payment payment, LocalDateTime requestAt, LocalDateTime performanceStartsAt) {
		validatePaymentIsPaid(payment);

		Refund refund = Refund.issue(requestAt, performanceStartsAt, payment.getAmount());
		refund.validateRefundable();

		payment.assignRefund(refund);
		return refund;
	}

	private void validatePaymentIsPaid(Payment payment) {
		if (!payment.isSucceeded()) {
			throw new InvalidPaymentStatusException("환불", payment.getStatus(), PaymentStatus.SUCCEEDED);
		}
		if (payment.hasRefunded()) {
			throw new RefundAlreadyExistsException();
		}
	}

}
