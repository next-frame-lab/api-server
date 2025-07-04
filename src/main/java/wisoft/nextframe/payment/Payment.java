package wisoft.nextframe.payment;

import java.time.LocalDateTime;

import lombok.Getter;
import wisoft.nextframe.common.Money;
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
			throw new RuntimeException("결제를 위해서는 예매 정보가 필요합니다.");
		}
		if (amount == null || !amount.isPositive()) {
			throw new RuntimeException("결제 금액은 0보다 커야 합니다.");
		}
		if (amount.isGreaterThan(MAX_AMOUNT)) {
			throw new RuntimeException("결제 금액은 최대 1천만 원 미만이어야 합니다.");
		}

		return new Payment(amount, requestedAt, reservationId);
	}

	public Refund refund(LocalDateTime requestAt, LocalDateTime contentStartsAt) {
		validatePaymentIsPaid();
		if (this.currentRefund != null) {
			throw new IllegalStateException("이미 환불된 결제입니다.");
		}
		Refund newRefund = Refund.issue(requestAt, contentStartsAt, amount);
		newRefund.validateRefundable();

		this.currentRefund = newRefund;
		return currentRefund;
	}

	private void validatePaymentIsPaid() {
		if (!isPaid()) {
			throw new RuntimeException("결제되지 않은 건은 환불할 수 없습니다.");
		}
	}

	public void markAsSucceed() {
		if (this.status == PaymentStatus.SUCCEEDED || this.status == PaymentStatus.PAID) {
			throw new RuntimeException("이미 결제 성공 처리된 건입니다.");
		}

		if (this.status == PaymentStatus.FAILED) {
			throw new RuntimeException("결제 요청 상태가 아닙니다. 결제 요청 후에만 성공 처리가 가능합니다.");
		}
		this.status = PaymentStatus.SUCCEEDED;
	}

	public void confirm() {
		if (this.status != PaymentStatus.SUCCEEDED) {
			throw new IllegalStateException("결제가 완료되지 않았습니다.");
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
