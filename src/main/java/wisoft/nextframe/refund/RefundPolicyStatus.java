package wisoft.nextframe.refund;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public enum RefundPolicyStatus {
	REFUND_DENY(0.0),
	PARTIAL_REFUND(0.5),
	FULL_REFUND(1.0);

	private final double ratio;

	RefundPolicyStatus(double ratio) {
		this.ratio = ratio;
	}

	public BigDecimal calculateRefund(BigDecimal amount) {
		return amount.multiply(BigDecimal.valueOf(ratio));
	}

	public boolean isRefundable() {
		return ratio > 0;
	}

	public static RefundPolicyStatus from(LocalDateTime requestAt, LocalDateTime contentStartsAt) {
		LocalDateTime oneHourBefore = contentStartsAt.minusHours(1);
		LocalDateTime twentyFourHoursBefore = contentStartsAt.minusHours(24);

		if (requestAt.isBefore(twentyFourHoursBefore)) {
			return RefundPolicyStatus.FULL_REFUND;
		} else if (requestAt.isBefore(oneHourBefore)) {
			return RefundPolicyStatus.PARTIAL_REFUND;
		}
		return RefundPolicyStatus.REFUND_DENY;
	}

}
