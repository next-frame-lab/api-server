package wisoft.nextframe.payment.domain.refund;

import static java.time.temporal.ChronoUnit.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;

import wisoft.nextframe.common.Money;

public enum RefundPolicyStatus {
	REFUND_FULL(1.0, 7),
	REFUND_80_PERCENT(0.8, 3),
	REFUND_60_PERCENT(0.6, 1),
	NON_REFUNDABLE(0.0, 0);

	private final double refundRate;
	private final int daysBefore;

	RefundPolicyStatus(double refundRate, int daysBefore) {
		this.refundRate = refundRate;
		this.daysBefore = daysBefore;
	}

	public Money calculateRefundAmount(Money amount) {
		return amount.multiply(BigDecimal.valueOf(refundRate));
	}

	public boolean isRefundable() {
		return refundRate > 0;
	}

	public static RefundPolicyStatus from(LocalDateTime requestAt, LocalDateTime performanceStartsAt) {
		long daysBetween = DAYS.between(requestAt.toLocalDate(), performanceStartsAt.toLocalDate());

		return Arrays.stream(values())
			.sorted(Comparator.comparingInt((RefundPolicyStatus status) -> status.daysBefore).reversed())
			.filter(status -> daysBetween >= status.daysBefore)
			.findFirst()
			.orElse(NON_REFUNDABLE);

	}

}
