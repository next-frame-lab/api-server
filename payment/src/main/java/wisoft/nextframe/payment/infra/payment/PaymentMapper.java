package wisoft.nextframe.payment.infra.payment;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.payment.common.Money;
import wisoft.nextframe.payment.common.mapper.EntityMapper;
import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.payment.domain.payment.PaymentId;
import wisoft.nextframe.payment.domain.ReservationId;
import wisoft.nextframe.payment.domain.refund.Refund;
import wisoft.nextframe.payment.infra.refund.JpaRefundRepository;
import wisoft.nextframe.payment.infra.refund.RefundMapper;

@Component
@RequiredArgsConstructor
public class PaymentMapper implements EntityMapper<Payment, PaymentEntity> {

	private final JpaRefundRepository jpaRefundRepository;
	private final RefundMapper refundMapper;

	@Override
	public Payment toDomain(PaymentEntity entity) {
		Refund refund = jpaRefundRepository.findByPaymentId(entity.getId())
			.map(refundMapper::toDomain)
			.orElse(null);

		return Payment.reconstruct(
			PaymentId.of(entity.getId()),
			ReservationId.of(entity.getReservationId()),
			Money.of(entity.getTotalAmount()),
			entity.getRequestedAt(),
			entity.getStatus(),
			refund
		);
	}

	@Override
	public PaymentEntity toEntity(Payment payment) {
		return PaymentEntity.builder()
			.id(payment.getId().getValue())
			.reservationId(payment.getReservationId().value())
			.totalAmount(payment.getAmount().getValue().intValue())
			.status(payment.getStatus())
			.requestedAt(payment.getRequestedAt())
			.build();
	}

}
