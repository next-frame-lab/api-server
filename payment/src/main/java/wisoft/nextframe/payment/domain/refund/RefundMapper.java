package wisoft.nextframe.payment.domain.refund;

import java.util.UUID;

import wisoft.nextframe.payment.common.Money;

// 도메인에는 없고 엔티티에는 있는 필드가 있습니다. 추후 검토 필요
public class RefundMapper {

	public Refund toDomain(RefundEntity entity) {
		return Refund.reconstruct(
			RefundId.of(entity.getId()),
			Money.of(entity.getRefundAmount()),
			RefundStatus.valueOf(entity.getStatus()),
			RefundPolicyStatus.valueOf(entity.getRefundPolicy()),
			entity.getRequestedAt(),
			entity.getCompletedAt()
		);
	}

	public RefundEntity toEntity(Refund domain, UUID paymentId, String reason) {
		return RefundEntity.builder()
			.id(domain.getRefundId().getValue())
			.paymentId(paymentId)
			.refundAmount(domain.getRefundedAmount().getValue().intValue())
			.status(domain.getStatus().name())
			.refundPolicy(domain.getPolicyStatus().name())
			.reason(reason)
			.requestedAt(domain.getRequestedAt())
			.completedAt(domain.getCompletedAt())
			.build();
	}
}
