package wisoft.nextframe.payment.infra.payment;

import org.springframework.stereotype.Component;

import wisoft.nextframe.payment.common.Money;
import wisoft.nextframe.payment.common.mapper.EntityMapper;
import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.payment.domain.payment.PaymentId;
import wisoft.nextframe.schedulereservationticketing.reservation.ReservationId;

@Component
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
	public PaymentEntity toEntity(Payment payment) {
		return PaymentEntity.builder()
			.id(payment.getId().getValue())
			.reservationId(payment.getReservationId().getValue())
			.totalAmount(payment.getAmount().getValue().intValue())
			.status(payment.getStatus())
			.requestedAt(payment.getRequestedAt())
			.build();
	}

}
