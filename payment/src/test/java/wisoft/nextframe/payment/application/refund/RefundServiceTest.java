package wisoft.nextframe.payment.application.refund;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import wisoft.nextframe.payment.application.payment.exception.PaymentGatewayTemporarilyUnavailableException;
import wisoft.nextframe.payment.application.payment.outbox.cancel.ReservationCancelOutboxService;
import wisoft.nextframe.payment.application.payment.port.output.PaymentGateway;
import wisoft.nextframe.payment.application.refund.RefundTransactionService.RefundPrepareResult;
import wisoft.nextframe.payment.common.Money;
import wisoft.nextframe.payment.domain.ReservationId;
import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.payment.domain.payment.PaymentId;
import wisoft.nextframe.payment.domain.payment.PaymentStatus;
import wisoft.nextframe.payment.domain.refund.Refund;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefundService 단위 테스트")
class RefundServiceTest {

	@Mock
	PaymentGateway paymentGateway;

	@Mock
	RefundTransactionService refundTransactionService;

	@Mock
	ReservationCancelOutboxService reservationCancelOutboxService;

	@InjectMocks
	RefundService refundService;

	private static final UUID PAYMENT_ID = UUID.randomUUID();
	private static final ReservationId RESERVATION_ID = ReservationId.of(UUID.randomUUID());
	private static final String REASON = "개인 사정";

	private Payment succeededPayment() {
		return Payment.reconstruct(
			PaymentId.of(PAYMENT_ID),
			RESERVATION_ID,
			Money.of(10_000),
			LocalDateTime.now(),
			PaymentStatus.SUCCEEDED,
			"pk_test_key",
			null
		);
	}

	private Refund newRefund() {
		return Refund.issue(LocalDateTime.now(), LocalDateTime.now().plusDays(10), Money.of(10_000));
	}

	@Test
	@DisplayName("이미 환불된 결제면 PG를 호출하지 않고 기존 환불을 반환한다")
	void refund_alreadyRefunded_skipsGatewayCall() {
		Refund existing = newRefund();
		given(refundTransactionService.prepareRefund(PAYMENT_ID))
			.willReturn(RefundPrepareResult.alreadyRefunded(existing));

		Refund result = refundService.refund(PAYMENT_ID, REASON);

		assertThat(result).isSameAs(existing);
		then(paymentGateway).shouldHaveNoInteractions();
		then(refundTransactionService).should(never()).saveRequested(any(), any(), any());
	}

	@Test
	@DisplayName("정상 환불 시 PG 호출 전에 Refund를 선저장하고, 성공하면 완료 처리와 예약 취소를 수행한다")
	void refund_success_savesRequestedBeforeGatewayCall() {
		Payment payment = succeededPayment();
		Refund refund = newRefund();
		Refund completed = newRefund();
		PaymentGateway.PaymentCancelResult cancelResult =
			new PaymentGateway.PaymentCancelResult(true, 10_000, "txKey", null, null);

		given(refundTransactionService.prepareRefund(PAYMENT_ID))
			.willReturn(RefundPrepareResult.prepared(payment, refund));
		given(paymentGateway.cancelPayment("pk_test_key", RESERVATION_ID.value().toString(),
			10_000, REASON)).willReturn(cancelResult);
		given(refundTransactionService.completeRefund(PAYMENT_ID, refund, REASON)).willReturn(completed);

		Refund result = refundService.refund(PAYMENT_ID, REASON);

		assertThat(result).isSameAs(completed);
		then(refundTransactionService).should().saveRequested(PAYMENT_ID, refund, REASON);
		then(refundTransactionService).should().completeRefund(PAYMENT_ID, refund, REASON);
		then(reservationCancelOutboxService).should()
			.cancelOrEnqueue(PAYMENT_ID, RESERVATION_ID.value());
	}

	@Test
	@DisplayName("PG 호출이 CB로 차단되면 선저장까지만 하고 완료 처리 없이 예외를 다시 던진다")
	void refund_gatewayTemporarilyUnavailable_rethrowsWithoutCompleting() {
		Payment payment = succeededPayment();
		Refund refund = newRefund();
		PaymentGatewayTemporarilyUnavailableException cbException =
			new PaymentGatewayTemporarilyUnavailableException("cancel", new RuntimeException("CB OPEN"));

		given(refundTransactionService.prepareRefund(PAYMENT_ID))
			.willReturn(RefundPrepareResult.prepared(payment, refund));
		given(paymentGateway.cancelPayment("pk_test_key", RESERVATION_ID.value().toString(),
			10_000, REASON)).willThrow(cbException);

		assertThatThrownBy(() -> refundService.refund(PAYMENT_ID, REASON))
			.isSameAs(cbException);

		then(refundTransactionService).should().saveRequested(PAYMENT_ID, refund, REASON);
		then(refundTransactionService).should(never()).completeRefund(any(), any(), any());
		then(reservationCancelOutboxService).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("PG가 실패 응답을 반환하면 선저장까지만 하고 RefundCancelFailedException을 던진다")
	void refund_gatewayReturnsFailure_throwsRefundCancelFailedException() {
		Payment payment = succeededPayment();
		Refund refund = newRefund();
		PaymentGateway.PaymentCancelResult cancelResult =
			new PaymentGateway.PaymentCancelResult(false, 0, null, "ERR", "실패");

		given(refundTransactionService.prepareRefund(PAYMENT_ID))
			.willReturn(RefundPrepareResult.prepared(payment, refund));
		given(paymentGateway.cancelPayment("pk_test_key", RESERVATION_ID.value().toString(),
			10_000, REASON)).willReturn(cancelResult);

		assertThatThrownBy(() -> refundService.refund(PAYMENT_ID, REASON))
			.isInstanceOf(RefundCancelFailedException.class);

		then(refundTransactionService).should().saveRequested(PAYMENT_ID, refund, REASON);
		then(refundTransactionService).should(never()).completeRefund(any(), any(), any());
	}
}
