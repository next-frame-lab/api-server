package wisoft.nextframe.payment.refund;

import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import wisoft.nextframe.common.Money;
import wisoft.nextframe.payment.Payment;
import wisoft.nextframe.payment.refund.Refund;

public class TestRefundFactory {

	private static final Money DEFAULT_AMOUNT = Money.of(10_000);
	private static final LocalDateTime CONTENT_START_TIME = LocalDateTime.of(2025, 7, 10, 20, 0);
	private static final LocalDateTime REFUND_REQUEST_TIME = LocalDateTime.of(2025, 7, 8, 12, 0);

	public static Payment paidPayment() {
		Payment payment = mock(Payment.class);
		when(payment.isPaid()).thenReturn(true);
		when(payment.getAmount()).thenReturn(DEFAULT_AMOUNT);
		return payment;
	}

	public static Refund requested() {
		return Refund.issue(REFUND_REQUEST_TIME, CONTENT_START_TIME, DEFAULT_AMOUNT);
	}

	public static Refund fullRefund() {
		LocalDateTime requestTime = CONTENT_START_TIME.minusHours(30);
		return Refund.issue(requestTime, CONTENT_START_TIME, DEFAULT_AMOUNT);
	}

	public static Refund partialRefund() {
		LocalDateTime requestTime = CONTENT_START_TIME.minusHours(2);
		return Refund.issue(requestTime, CONTENT_START_TIME, DEFAULT_AMOUNT);
	}

	public static Refund denyRefund() {
		LocalDateTime requestTime = CONTENT_START_TIME.minusHours(1);
		Refund refund = Refund.issue(requestTime, CONTENT_START_TIME, DEFAULT_AMOUNT);
		refund.reject();
		return refund;
	}

	public static Money defaultAmount() {
		return DEFAULT_AMOUNT;
	}
}
