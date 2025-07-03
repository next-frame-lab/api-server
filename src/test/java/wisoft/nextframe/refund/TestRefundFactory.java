package wisoft.nextframe.refund;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TestRefundFactory {

	private static final BigDecimal DEFAULT_AMOUNT = BigDecimal.valueOf(10_000);
	private static final LocalDateTime CONTENT_START_TIME = LocalDateTime.of(2025, 7, 10, 20, 0);
	private static final LocalDateTime REFUND_REQUEST_TIME = LocalDateTime.of(2025, 7, 8, 12, 0);

	public static Refund requested() {
		return new Refund(REFUND_REQUEST_TIME, CONTENT_START_TIME, DEFAULT_AMOUNT);
	}

	public static Refund fullRefund() {
		LocalDateTime requestTime = CONTENT_START_TIME.minusHours(30);
		return new Refund(requestTime, CONTENT_START_TIME, DEFAULT_AMOUNT);
	}

	public static Refund partialRefund() {
		LocalDateTime requestTime = CONTENT_START_TIME.minusHours(2);
		return new Refund(requestTime, CONTENT_START_TIME, DEFAULT_AMOUNT);
	}

	public static LocalDateTime contentStartTime() {
		return CONTENT_START_TIME;
	}

	public static BigDecimal defaultAmount() {
		return DEFAULT_AMOUNT;
	}

}
