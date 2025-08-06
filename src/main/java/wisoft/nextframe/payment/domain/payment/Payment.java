package wisoft.nextframe.payment.domain.payment;

import java.time.LocalDateTime;

import lombok.Getter;
import wisoft.nextframe.common.Money;
import wisoft.nextframe.common.exception.InvalidAmountException;
import wisoft.nextframe.payment.domain.payment.exception.InvalidPaymentStatusException;
import wisoft.nextframe.payment.domain.payment.exception.MissingReservationException;
import wisoft.nextframe.payment.domain.payment.exception.PaymentAlreadySucceededException;
import wisoft.nextframe.payment.domain.payment.exception.TooLargeAmountException;
import wisoft.nextframe.payment.domain.refund.Refund;
import wisoft.nextframe.reservation.domain.ReservationId;

@Getter
public class Payment {
	private static final Money MAX_AMOUNT = Money.of(100000);

	private final PaymentId id;
	private final ReservationId reservationId;
	private final Money amount;
	private PaymentStatus status;
	private final LocalDateTime requestedAt;
	private Refund currentRefund;

	private Payment(PaymentId id, Money amount, LocalDateTime requestedAt, ReservationId reservationId) {
		this.id = id;
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

		return new Payment(PaymentId.of(), amount, requestedAt, reservationId);
	}

	public static Payment reconstruct(
		PaymentId id,
		ReservationId reservationId,
		Money amount,
		LocalDateTime requestedAt,
		PaymentStatus status,
		Refund refund
	) {
		Payment payment = new Payment(id, amount, requestedAt, reservationId);
		payment.status = status;  // 상태는 직접 주입
		payment.currentRefund = refund; // 환불 이력도 복원

		return payment;
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
