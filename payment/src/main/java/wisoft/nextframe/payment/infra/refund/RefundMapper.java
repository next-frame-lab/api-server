package wisoft.nextframe.payment.infra.refund;

import java.util.UUID;

import org.springframework.stereotype.Component;

import wisoft.nextframe.payment.common.Money;
import wisoft.nextframe.payment.domain.refund.Refund;
import wisoft.nextframe.payment.domain.refund.RefundId;
import wisoft.nextframe.payment.domain.refund.RefundPolicyStatus;
import wisoft.nextframe.payment.domain.refund.RefundStatus;

@Component
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
