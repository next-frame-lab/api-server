package wisoft.nextframe.payment.infra.refund;

import org.springframework.stereotype.Component;

import wisoft.nextframe.payment.common.Money;
import wisoft.nextframe.payment.domain.refund.Refund;
import wisoft.nextframe.payment.domain.refund.RefundId;
import wisoft.nextframe.payment.domain.refund.RefundPolicyStatus;
import wisoft.nextframe.payment.domain.refund.RefundStatus;
import wisoft.nextframe.payment.infra.payment.PaymentEntity;

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

	public RefundEntity toEntity(Refund domain, PaymentEntity payment, String reason) {
		return RefundEntity.builder()
			.id(domain.getRefundId().getValue())
			.payment(payment)
			.refundAmount(domain.getRefundedAmount().getValue().intValue())
			.status(domain.getStatus().name())
			.refundPolicy(domain.getPolicyStatus().name())
			.reason(reason)
			.requestedAt(domain.getRequestedAt())
			.completedAt(domain.getCompletedAt())
			.build();
	}
}
