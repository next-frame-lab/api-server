package wisoft.nextframe.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Payment {
	private final UUID reservationId;
	private BigDecimal amount;
	private PaymentStatus status;
	private LocalDateTime requestedAt;

	private static final BigDecimal MAX_AMOUNT = BigDecimal.valueOf(10_000_000);

	public Payment(BigDecimal amount, LocalDateTime requestedAt, UUID reservationId) {
		this.amount = amount;
		this.reservationId = reservationId;
		this.status = PaymentStatus.REQUESTED; // 초기 상태는 REQUESTED
		this.requestedAt = requestedAt;
	}

	public static Payment request(BigDecimal amount, UUID reservationId, LocalDateTime requestedAt) {
		if (reservationId == null) {
			throw new RuntimeException("결제를 위해서는 예매 정보가 필요합니다.");
		}
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new RuntimeException("결제 금액은 0보다 커야 합니다.");
		}
		if (amount.compareTo(MAX_AMOUNT) >= 0) {
			throw new RuntimeException("결제 금액은 최대 1천만 원 미만이어야 합니다.");
		}

		return new Payment(amount, requestedAt, reservationId);
	}

	public static Payment request(BigDecimal amount, UUID reservationId) {
		return request(amount, reservationId, LocalDateTime.now());
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

	public void confirmPayment() {
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

	public PaymentStatus getStatus() {
		return status;
	}

	public UUID getReservationId() {
		return reservationId;
	}

	public BigDecimal getAmount() {
		return amount;
	}
}
