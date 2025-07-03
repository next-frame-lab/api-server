package wisoft.nextframe.refund;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import wisoft.nextframe.payment.Payment;

// 상태 결정만 담당, 값 객체
public class Refund {
	private final BigDecimal amount;
	private final LocalDateTime requestAt;
	private final LocalDateTime contentStartsAt;
	private RefundStatus status;
	private final RefundPolicyStatus policyStatus;

	public Refund(LocalDateTime requestAt, LocalDateTime contentStartsAt, BigDecimal originalAmount) {
		this.requestAt = requestAt;
		this.contentStartsAt = contentStartsAt;
		this.policyStatus = RefundPolicyStatus.from(requestAt, contentStartsAt);
		this.status = RefundStatus.REQUESTED;
		this.amount = policyStatus.calculateRefund(originalAmount);
	}

	public static Refund refund(Payment payment, LocalDateTime requestAt, LocalDateTime contentStartsAt) {
		validatePaymentIsPaid(payment);

		Refund candidate = new Refund(requestAt, contentStartsAt, payment.getAmount());

		validateRefundable(candidate);

		candidate.approve();
		return candidate;
	}

	private static void validatePaymentIsPaid(Payment payment) {
		if (!payment.isPaid()) {
			throw new RuntimeException("결제되지 않은 건은 환불할 수 없습니다.");
		}
	}

	private static void validateRefundable(Refund refund) {
		if (!refund.isRefundable()) {
			throw new RuntimeException("공연 시작 1시간 전에는 환불할 수 없습니다.");
		}
	}


	public void approve() {
		if (this.status != RefundStatus.REQUESTED) {
			throw new RuntimeException("승인은 REQUESTED 상태에서만 가능합니다.");
		}
		this.status = RefundStatus.APPROVED;
	}

	public void reject() {
		if (this.status != RefundStatus.REQUESTED) {
			throw new RuntimeException("거절은 REQUESTED 상태에서만 가능합니다.");
		}
		this.status = RefundStatus.REJECTED;
	}

	public boolean isRefundable() {
		return policyStatus.isRefundable();
	}

	public RefundPolicyStatus getPolicyStatus() {
		return policyStatus;
	}

	public RefundStatus getStatus() {
		return status;
	}

	public BigDecimal getAmount() {
		return amount;
	}
}
