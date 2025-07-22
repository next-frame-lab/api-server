package wisoft.nextframe.domain.payment.refund;

import java.time.LocalDateTime;

import lombok.Getter;
import wisoft.nextframe.common.Money;
import wisoft.nextframe.domain.payment.refund.exception.InvalidRefundStatusException;
import wisoft.nextframe.domain.payment.refund.exception.NotRefundableException;

// 상태 결정만 담당, 값 객체
@Getter
public class Refund {

	private final RefundId refundId;
	private final Money refundedAmount;
	private RefundStatus status;
	private final RefundPolicyStatus policyStatus;
	private final LocalDateTime requestedAt;
	private LocalDateTime completedAt;

	private Refund(RefundId refundId, LocalDateTime requestedAt, RefundPolicyStatus policyStatus, Money originalAmount) {
		this.refundId = refundId;
		this.requestedAt = requestedAt;
		this.policyStatus = policyStatus;
		this.status = RefundStatus.REQUESTED;
		this.refundedAmount = policyStatus.calculateRefundAmount(originalAmount);
	}

	public static Refund issue(LocalDateTime requestAt, LocalDateTime contentStartsAt, Money originalAmount) {
		RefundPolicyStatus policy = RefundPolicyStatus.from(requestAt, contentStartsAt);
		RefundId refundId = RefundId.generate();
		return new Refund(refundId, requestAt, policy, originalAmount);
	}

	public void validateRefundable() {
		if (!policyStatus.isRefundable()) {
			throw new NotRefundableException();
		}
	}

	public void approve() {
		if (this.status != RefundStatus.REQUESTED) {
			throw new InvalidRefundStatusException("승인");
		}
		this.status = RefundStatus.APPROVED;
	}

	public void reject() {
		if (this.status != RefundStatus.REQUESTED) {
			throw new InvalidRefundStatusException("거절");
		}
		this.status = RefundStatus.REJECTED;
	}

	public void markAsCompleted() {
		this.status = RefundStatus.COMPLETED;
		this.completedAt = LocalDateTime.now();
	}

}
