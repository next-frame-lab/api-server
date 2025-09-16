package wisoft.nextframe.payment.domain.fixture;

import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import wisoft.nextframe.payment.common.Money;
import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.payment.domain.refund.Refund;

public class TestRefundFactory {

	private static final Money DEFAULT_AMOUNT = Money.of(10_000);
	private static final LocalDateTime CONTENT_START_TIME = LocalDateTime.of(2025, 7, 10, 20, 0);
	private static final LocalDateTime REFUND_REQUEST_TIME = LocalDateTime.of(2025, 7, 8, 12, 0);

	public static Payment paidPayment() {
		Payment payment = mock(Payment.class);
		when(payment.isSucceeded()).thenReturn(true);
		when(payment.getAmount()).thenReturn(DEFAULT_AMOUNT);
		return payment;
	}

	public static Refund requested() {
		return Refund.issue(REFUND_REQUEST_TIME, CONTENT_START_TIME, DEFAULT_AMOUNT);
	}

	public static Refund refundFull() {
		LocalDateTime requestTime = CONTENT_START_TIME.minusDays(8);
		return Refund.issue(requestTime, CONTENT_START_TIME, DEFAULT_AMOUNT);
	}

	public static Refund refund80percent() {
		LocalDateTime requestTime = CONTENT_START_TIME.minusDays(4);
		return Refund.issue(requestTime, CONTENT_START_TIME, DEFAULT_AMOUNT);
	}

	public static Refund refund60percent() {
		LocalDateTime requestTime = CONTENT_START_TIME.minusDays(2);
		return Refund.issue(requestTime, CONTENT_START_TIME, DEFAULT_AMOUNT);
	}

	public static Refund refundDeny() {
		LocalDateTime requestTime = CONTENT_START_TIME.minusHours(1);
		return Refund.issue(requestTime, CONTENT_START_TIME, DEFAULT_AMOUNT);
	}

	public static Money defaultAmount() {
		return DEFAULT_AMOUNT;
	}
}
