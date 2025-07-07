package wisoft.nextframe.payment;

import java.time.LocalDateTime;

import lombok.Getter;
import wisoft.nextframe.common.Money;
import wisoft.nextframe.common.exception.InvalidAmountException;
import wisoft.nextframe.payment.exception.InvalidPaymentStatusException;
import wisoft.nextframe.payment.exception.MissingReservationException;
import wisoft.nextframe.payment.exception.PaymentAlreadySucceededException;
import wisoft.nextframe.payment.exception.TooLargeAmountException;
import wisoft.nextframe.payment.refund.Refund;
import wisoft.nextframe.reservation.ReservationId;

@Getter
public class Payment {
	private static final Money MAX_AMOUNT = Money.of(10_000_000);

	private final ReservationId reservationId;
	private final Money amount;
	private PaymentStatus status;
	private final LocalDateTime requestedAt;
	private Refund currentRefund;

	private Payment(Money amount, LocalDateTime requestedAt, ReservationId reservationId) {
		this.amount = amount;
		this.reservationId = reservationId;
		this.status = PaymentStatus.REQUESTED; // 초기 상태는 REQUESTED
		this.requestedAt = requestedAt;
	}

	public static Payment request(Money amount, ReservationId reservationId, LocalDateTime requestedAt) {
		if (reservationId == null) {
			throw new MissingReservationException();
		}
		if (amount == null || !amount.isPositive()) {
			throw new InvalidAmountException();
		}
		if (amount.isGreaterThan(MAX_AMOUNT)) {
			throw new TooLargeAmountException();
		}

		return new Payment(amount, requestedAt, reservationId);
	}

	public boolean hasRefunded() {
		return this.currentRefund != null;
	}

	public void assignRefund(Refund refund) {
		this.currentRefund = refund;
	}

	public void markAsSucceed() {
		if (this.status == PaymentStatus.SUCCEEDED || this.status == PaymentStatus.PAID) {
			throw new PaymentAlreadySucceededException();
		}

		if (this.status == PaymentStatus.FAILED) {
			throw new InvalidPaymentStatusException("결제 성공", status, PaymentStatus.REQUESTED);
		}
		this.status = PaymentStatus.SUCCEEDED;
	}

	public void confirm() {
		if (this.status != PaymentStatus.SUCCEEDED) {
			throw new InvalidPaymentStatusException("결제 확정", status, PaymentStatus.SUCCEEDED);
		}
		this.status = PaymentStatus.PAID;
	}

	public void fail() {
		this.status = PaymentStatus.FAILED;
	}

	public boolean isPaid() {
		return this.status == PaymentStatus.PAID;
	}

	public void checkTimeout() {
		if (this.status == PaymentStatus.REQUESTED
			&& requestedAt.plusMinutes(10).isBefore(LocalDateTime.now())) {
			this.status = PaymentStatus.FAILED;
		}
	}

}
