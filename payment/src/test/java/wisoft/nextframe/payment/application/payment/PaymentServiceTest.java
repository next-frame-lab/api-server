package wisoft.nextframe.payment.application.payment;

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

import wisoft.nextframe.payment.application.payment.exception.PaymentGatewayExternalCallFailedException;
import wisoft.nextframe.payment.application.payment.exception.PaymentGatewayTemporarilyUnavailableException;
import wisoft.nextframe.payment.application.payment.exception.ReservationExpiredException;
import wisoft.nextframe.payment.application.payment.port.output.PaymentGateway;
import wisoft.nextframe.payment.application.payment.port.output.ReservationReader;
import wisoft.nextframe.payment.common.Money;
import wisoft.nextframe.payment.domain.ReservationId;
import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.payment.presentation.payment.dto.PaymentConfirmRequest;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService 단위 테스트")
class PaymentServiceTest {

	@Mock
	PaymentGateway paymentGateway;

	@Mock
	ReservationReader reservationReader;

	@Mock
	PaymentTransactionService paymentTransactionService;

	@InjectMocks
	PaymentService paymentService;

	private static final UUID RESERVATION_UUID = UUID.randomUUID();
	private static final ReservationId RESERVATION_ID = ReservationId.of(RESERVATION_UUID);
	private static final PaymentConfirmRequest REQUEST =
		new PaymentConfirmRequest("paymentKey", RESERVATION_UUID.toString(), 10_000);

	@Test
	@DisplayName("예약이 결제 불가 상태면 PG를 호출하지 않고 예외를 던진다")
	void confirmPayment_reservationNotPayable_throwsWithoutCallingGateway() {
		given(reservationReader.isPayable(RESERVATION_ID)).willReturn(false);

		assertThatThrownBy(() -> paymentService.confirmPayment(REQUEST))
			.isInstanceOf(ReservationExpiredException.class);

		then(paymentTransactionService).shouldHaveNoInteractions();
		then(paymentGateway).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("PG 호출이 정상 응답하면 결과를 applyConfirmResult로 위임한다")
	void confirmPayment_success_delegatesToApplyConfirmResult() {
		Payment requested = Payment.request(Money.of(10_000), RESERVATION_ID, LocalDateTime.now());
		Payment approved = Payment.request(Money.of(10_000), RESERVATION_ID, LocalDateTime.now());
		PaymentGateway.PaymentConfirmResult result =
			new PaymentGateway.PaymentConfirmResult(true, 10_000, null, null);

		given(reservationReader.isPayable(RESERVATION_ID)).willReturn(true);
		given(paymentTransactionService.createRequested(RESERVATION_ID, 10_000)).willReturn(requested);
		given(paymentGateway.confirmPayment("paymentKey", RESERVATION_UUID.toString(), 10_000))
			.willReturn(result);
		given(paymentTransactionService.applyConfirmResult(REQUEST, result)).willReturn(approved);

		Payment returned = paymentService.confirmPayment(REQUEST);

		assertThat(returned).isSameAs(approved);
		then(paymentTransactionService).should(never()).handlePaymentFailure(any());
	}

	@Test
	@DisplayName("CB OPEN으로 PG 호출이 차단되면 선저장된 Payment를 실패 처리하고 예외를 다시 던진다")
	void confirmPayment_circuitBreakerOpen_marksFailedAndRethrows() {
		Payment requested = Payment.request(Money.of(10_000), RESERVATION_ID, LocalDateTime.now());
		PaymentGatewayTemporarilyUnavailableException cbException =
			new PaymentGatewayTemporarilyUnavailableException("confirm", new RuntimeException("CB OPEN"));

		given(reservationReader.isPayable(RESERVATION_ID)).willReturn(true);
		given(paymentTransactionService.createRequested(RESERVATION_ID, 10_000)).willReturn(requested);
		given(paymentGateway.confirmPayment("paymentKey", RESERVATION_UUID.toString(), 10_000))
			.willThrow(cbException);

		assertThatThrownBy(() -> paymentService.confirmPayment(REQUEST))
			.isSameAs(cbException);

		then(paymentTransactionService).should().handlePaymentFailure(requested);
		then(paymentTransactionService).should(never()).applyConfirmResult(any(), any());
	}

	@Test
	@DisplayName("PG 외부 호출 실패 시 선저장된 Payment를 실패 처리하고 예외를 다시 던진다")
	void confirmPayment_externalCallFailed_marksFailedAndRethrows() {
		Payment requested = Payment.request(Money.of(10_000), RESERVATION_ID, LocalDateTime.now());
		PaymentGatewayExternalCallFailedException callFailedException =
			new PaymentGatewayExternalCallFailedException("confirm", new RuntimeException("timeout"));

		given(reservationReader.isPayable(RESERVATION_ID)).willReturn(true);
		given(paymentTransactionService.createRequested(RESERVATION_ID, 10_000)).willReturn(requested);
		given(paymentGateway.confirmPayment("paymentKey", RESERVATION_UUID.toString(), 10_000))
			.willThrow(callFailedException);

		assertThatThrownBy(() -> paymentService.confirmPayment(REQUEST))
			.isSameAs(callFailedException);

		then(paymentTransactionService).should().handlePaymentFailure(requested);
	}
}
