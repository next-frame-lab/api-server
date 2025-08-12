package wisoft.nextframe.payment.infra.payment.persistence;

import static java.util.UUID.*;

import java.time.LocalDateTime;
import java.util.UUID;

import wisoft.nextframe.payment.common.Money;
import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.payment.domain.payment.PaymentId;
import wisoft.nextframe.payment.domain.payment.PaymentStatus;
import wisoft.nextframe.payment.infra.payment.PaymentEntity;
import wisoft.nextframe.schedulereservationticketing.reservation.domain.ReservationId;

public class PaymentEntityFixture {

	public static final UUID DEFAULT_PAYMENT_ID = randomUUID();
	public static final UUID DEFAULT_RESERVATION_ID = randomUUID();
	public static final int DEFAULT_AMOUNT = 50000;
	public static final String DEFAULT_STATUS = "REQUESTED";

	public static PaymentEntity sampleEntity() {
		return PaymentEntity.builder()
			.id(DEFAULT_PAYMENT_ID)
			.reservationId(DEFAULT_RESERVATION_ID)
			.totalAmount(DEFAULT_AMOUNT)
			.status(PaymentStatus.valueOf(DEFAULT_STATUS))
			.build();
	}

	public static Payment sampleDomain() {
		return Payment.reconstruct(
			PaymentId.of(DEFAULT_PAYMENT_ID),
			ReservationId.of(DEFAULT_RESERVATION_ID),
			Money.of(DEFAULT_AMOUNT),
			LocalDateTime.now(),
			PaymentStatus.valueOf(DEFAULT_STATUS),
			null
		);
	}
}
