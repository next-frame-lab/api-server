package wisoft.nextframe.payment.infra.payment;

import wisoft.nextframe.common.Money;
import wisoft.nextframe.common.mapper.EntityMapper;
import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.payment.domain.payment.PaymentId;
import wisoft.nextframe.reservation.domain.ReservationId;

public class PaymentMapper implements EntityMapper<Payment, PaymentEntity> {
	@Override
	public Payment toDomain(PaymentEntity entity) {
		return Payment.reconstruct(
			PaymentId.of(entity.getId()),
			ReservationId.of(entity.getReservationId()),
			Money.of(entity.getTotalAmount()),
			entity.getRequestedAt(),
			entity.getStatus(),
			null // 환불 이력은 아직 구현되지 않음
		);
	}

	@Override
	public PaymentEntity toEntity(Payment domain) {
		return new PaymentEntity(
			domain.getId().getValue(),
			domain.getReservationId().getValue(),
			domain.getAmount().getValue().intValue(),
			domain.getStatus(),
			domain.getRequestedAt(),
			null  // paymentMethod 나중에
		);
	}

}
