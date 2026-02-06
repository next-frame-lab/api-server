package wisoft.nextframe.payment.domain.payment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.ToString;
import wisoft.nextframe.payment.common.Money;
import wisoft.nextframe.payment.common.exception.InvalidAmountException;
import wisoft.nextframe.payment.domain.ReservationId;
import wisoft.nextframe.payment.domain.payment.event.DomainEvent;
import wisoft.nextframe.payment.domain.payment.event.PaymentApprovedEvent;
import wisoft.nextframe.payment.domain.payment.event.PaymentFailedEvent;
import wisoft.nextframe.payment.domain.payment.exception.InvalidPaymentStatusException;
import wisoft.nextframe.payment.domain.payment.exception.MissingReservationException;
import wisoft.nextframe.payment.domain.payment.exception.PaymentAlreadySucceededException;
import wisoft.nextframe.payment.domain.payment.exception.TooLargeAmountException;
import wisoft.nextframe.payment.domain.refund.Refund;

@Getter
@ToString
public class Payment {
	private static final Money MAX_AMOUNT = Money.of(10_000_000);

	private final PaymentId id;
	private final ReservationId reservationId;
	private final Money amount;
	private PaymentStatus status;
	private final LocalDateTime requestedAt;
	private LocalDateTime approvedAt;
	private LocalDateTime failedAt;
	private Refund currentRefund;
	private final List<DomainEvent> domainEvents = new ArrayList<>();

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

	public void approve() {
		if (this.status == PaymentStatus.SUCCEEDED) {
			throw new PaymentAlreadySucceededException();
		}

		if (this.status == PaymentStatus.FAILED) {
			throw new InvalidPaymentStatusException("결제 승인", status, PaymentStatus.REQUESTED);
		}
		this.status = PaymentStatus.SUCCEEDED;
		this.approvedAt = LocalDateTime.now();

		domainEvents.add(new PaymentApprovedEvent(this.id.getValue(), this.reservationId.value()));
	}

	public void fail() {
		if (this.status == PaymentStatus.SUCCEEDED) {
			throw new InvalidPaymentStatusException("결제 실패", status, PaymentStatus.REQUESTED);
		}
		if (this.status == PaymentStatus.FAILED) {
			return;
		}
		this.status = PaymentStatus.FAILED;
		this.failedAt = LocalDateTime.now();

		domainEvents.add(new PaymentFailedEvent(this.id.getValue(), this.reservationId.value()));
	}

	public boolean isSucceeded() {
		return this.status == PaymentStatus.SUCCEEDED;
	}

	public void checkTimeout() {
		if (this.status == PaymentStatus.REQUESTED
			&& requestedAt.plusMinutes(10).isBefore(LocalDateTime.now())) {
			this.status = PaymentStatus.FAILED;
		}
	}

	public List<DomainEvent> getDomainEvents() {
		return Collections.unmodifiableList(domainEvents);
	}

	public void clearDomainEvents() {
		domainEvents.clear();
	}

}