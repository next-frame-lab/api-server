package wisoft.nextframe.payment.domain.payment;

import static org.assertj.core.api.Assertions.*;
import static wisoft.nextframe.payment.domain.fixture.TestPaymentFactory.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wisoft.nextframe.payment.domain.payment.exception.InvalidPaymentStatusException;
import wisoft.nextframe.payment.domain.payment.exception.RefundAlreadyExistsException;
import wisoft.nextframe.payment.domain.refund.Refund;
import wisoft.nextframe.payment.domain.refund.RefundStatus;
import wisoft.nextframe.payment.domain.refund.exception.NotRefundableException;

@DisplayName("PaymentIssuer 도메인 서비스 테스트")
class PaymentIssuerTest {

	private final PaymentIssuer paymentIssuer = new PaymentIssuer();

	private static final LocalDateTime PERFORMANCE_START = LocalDateTime.of(2025, 8, 10, 19, 0);

	@Test
	@DisplayName("승인된 결제에 대해 환불을 발급하면 REQUESTED 상태의 Refund가 생성된다")
	void issueRefund_succeededPayment_createsRefund() {
		// given
		Payment payment = succeeded();
		LocalDateTime requestAt = PERFORMANCE_START.minusDays(8);

		// when
		Refund refund = paymentIssuer.issueRefund(payment, requestAt, PERFORMANCE_START);

		// then
		assertThat(refund.getStatus()).isEqualTo(RefundStatus.REQUESTED);
		assertThat(refund.getRefundedAmount().getValue()).isPositive();
		assertThat(payment.hasRefunded()).isTrue();
	}

	@Test
	@DisplayName("결제가 승인 상태가 아니면 환불 발급 시 예외가 발생한다")
	void issueRefund_notSucceededPayment_throwsException() {
		// given
		Payment payment = requested();
		LocalDateTime requestAt = PERFORMANCE_START.minusDays(8);

		// when & then
		assertThatThrownBy(() -> paymentIssuer.issueRefund(payment, requestAt, PERFORMANCE_START))
			.isInstanceOf(InvalidPaymentStatusException.class);
	}

	@Test
	@DisplayName("이미 환불된 결제에 대해 환불 발급 시 예외가 발생한다")
	void issueRefund_alreadyRefunded_throwsException() {
		// given
		Payment payment = succeeded();
		LocalDateTime requestAt = PERFORMANCE_START.minusDays(8);
		paymentIssuer.issueRefund(payment, requestAt, PERFORMANCE_START);

		// when & then
		assertThatThrownBy(() -> paymentIssuer.issueRefund(payment, requestAt, PERFORMANCE_START))
			.isInstanceOf(RefundAlreadyExistsException.class);
	}

	@Test
	@DisplayName("환불 불가 기간이면 예외가 발생한다")
	void issueRefund_nonRefundablePeriod_throwsException() {
		// given
		Payment payment = succeeded();
		LocalDateTime requestAt = PERFORMANCE_START.minusMinutes(30);

		// when & then
		assertThatThrownBy(() -> paymentIssuer.issueRefund(payment, requestAt, PERFORMANCE_START))
			.isInstanceOf(NotRefundableException.class);
	}

	@Test
	@DisplayName("환불 발급 후 Payment에 Refund가 할당된다")
	void issueRefund_assignsRefundToPayment() {
		// given
		Payment payment = succeeded();
		LocalDateTime requestAt = PERFORMANCE_START.minusDays(4);

		// when
		Refund refund = paymentIssuer.issueRefund(payment, requestAt, PERFORMANCE_START);

		// then
		assertThat(payment.getCurrentRefund()).isSameAs(refund);
	}
}
