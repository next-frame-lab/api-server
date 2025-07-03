package wisoft.nextframe.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TestPaymentFactory {

	private static final UUID DEFAULT_RESERVATION_ID = UUID.randomUUID();
	private static final BigDecimal DEFAULT_AMOUNT = BigDecimal.valueOf(10_000);

	public static Payment requested() {
		return Payment.request(DEFAULT_AMOUNT, DEFAULT_RESERVATION_ID);
	}

	public static Payment requestedWithAmount(BigDecimal amount) {
		return Payment.request(amount, DEFAULT_RESERVATION_ID);
	}

	public static Payment requestedAt(LocalDateTime requestTime) {
		return Payment.request(DEFAULT_AMOUNT, DEFAULT_RESERVATION_ID, requestTime);
	}

	public static Payment succeeded() {
		Payment payment = requested();
		payment.markAsSucceed();
		return payment;
	}

	public static Payment paid() {
		Payment payment = succeeded();
		payment.confirmPayment();
		return payment;
	}

	public static Payment failed() {
		Payment payment = requested();
		payment.fail();
		return payment;
	}

}
