package wisoft.nextframe.payment.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import wisoft.nextframe.common.Money;
import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.reservation.domain.ReservationId;

public class TestPaymentFactory {

	private static final ReservationId DEFAULT_RESERVATION_ID = ReservationId.of(UUID.randomUUID());
	private static final Money DEFAULT_AMOUNT = Money.of( 10_000);
	private static final LocalDateTime PAYMENT_REQUEST_TIME = LocalDateTime.of(2025, 7, 8, 12, 0);

	public static Payment requested() {
		return Payment.request(DEFAULT_AMOUNT, DEFAULT_RESERVATION_ID, PAYMENT_REQUEST_TIME);
	}

	public static Payment requestedWithAmount(Money amount) {
		return Payment.request(amount, DEFAULT_RESERVATION_ID, PAYMENT_REQUEST_TIME);
	}

	public static Payment requestedAt(LocalDateTime requestTime) {
		return Payment.request(DEFAULT_AMOUNT, DEFAULT_RESERVATION_ID, requestTime);
	}

	public static Payment requestedWithReservationId(ReservationId reservationId) {
		return Payment.request(DEFAULT_AMOUNT, reservationId, PAYMENT_REQUEST_TIME);
	}

	public static Payment succeeded() {
		Payment payment = requested();
		payment.markAsSucceed();
		return payment;
	}

	public static Payment paid() {
		Payment payment = succeeded();
		payment.confirm();
		return payment;
	}

	public static Payment failed() {
		Payment payment = requested();
		payment.fail();
		return payment;
	}

}
