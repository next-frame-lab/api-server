package wisoft.nextframe.domain.refund;

import java.time.LocalDateTime;

import lombok.Getter;
import wisoft.nextframe.common.Money;
import wisoft.nextframe.domain.refund.exception.InvalidRefundStatusException;
import wisoft.nextframe.domain.refund.exception.NotRefundableException;

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
		this.policyStatus = policyStatus;
		this.status = RefundStatus.REQUESTED;
		this.refundedAmount = policyStatus.calculateRefundAmount(originalAmount);
		this.requestedAt = requestedAt;
	}

	private Refund(
		RefundId refundId,
		Money refundedAmount,
		RefundStatus status,
		RefundPolicyStatus policyStatus,
		LocalDateTime requestedAt,
		LocalDateTime completedAt
	) {
		this.refundId = refundId;
		this.refundedAmount = refundedAmount;
		this.status = status;
		this.policyStatus = policyStatus;
		this.requestedAt = requestedAt;
		this.completedAt = completedAt;
	}

	public static Refund issue(LocalDateTime requestAt, LocalDateTime contentStartsAt, Money originalAmount) {
		RefundPolicyStatus policy = RefundPolicyStatus.from(requestAt, contentStartsAt);
		RefundId refundId = RefundId.generate();
		return new Refund(refundId, requestAt, policy, originalAmount);
	}

	public static Refund reconstruct(
		RefundId refundId,
		Money refundedAmount,
		RefundStatus status,
		RefundPolicyStatus policyStatus,
		LocalDateTime requestedAt,
		LocalDateTime completedAt
	) {
		return new Refund(refundId, refundedAmount, status, policyStatus, requestedAt, completedAt);
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
