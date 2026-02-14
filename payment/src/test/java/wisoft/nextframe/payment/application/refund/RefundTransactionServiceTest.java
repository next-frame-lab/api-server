package wisoft.nextframe.payment.application.refund;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import wisoft.nextframe.payment.application.payment.port.output.PaymentRepository;
import wisoft.nextframe.payment.application.payment.port.output.ReservationReader;
import wisoft.nextframe.payment.application.refund.RefundTransactionService.RefundPrepareResult;
import wisoft.nextframe.payment.application.refund.port.output.RefundRepository;
import wisoft.nextframe.payment.common.Money;
import wisoft.nextframe.payment.domain.ReservationId;
import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.payment.domain.payment.PaymentId;
import wisoft.nextframe.payment.domain.payment.PaymentIssuer;
import wisoft.nextframe.payment.domain.payment.PaymentNotFoundException;
import wisoft.nextframe.payment.domain.payment.PaymentStatus;
import wisoft.nextframe.payment.domain.refund.Refund;
import wisoft.nextframe.payment.domain.refund.RefundId;
import wisoft.nextframe.payment.domain.refund.RefundPolicyStatus;
import wisoft.nextframe.payment.domain.refund.RefundStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefundTransactionService 단위 테스트")
class RefundTransactionServiceTest {

	@Mock
	PaymentRepository paymentRepository;

	@Mock
	RefundRepository refundRepository;

	@Mock
	ReservationReader reservationReader;

	private final PaymentIssuer paymentIssuer = new PaymentIssuer();

	private RefundTransactionService refundTransactionService;

	private static final UUID PAYMENT_ID = UUID.randomUUID();
	private static final ReservationId RESERVATION_ID = ReservationId.of(UUID.randomUUID());
	private static final LocalDateTime PERFORMANCE_START = LocalDateTime.now().plusDays(10);
	private static final String REASON = "개인 사정";

	private Payment succeededPayment;

	@BeforeEach
	void setUp() {
		refundTransactionService = new RefundTransactionService(
			paymentRepository, refundRepository, reservationReader, paymentIssuer
		);

		succeededPayment = Payment.reconstruct(
			PaymentId.of(PAYMENT_ID),
			RESERVATION_ID,
			Money.of(10_000),
			LocalDateTime.now(),
			PaymentStatus.SUCCEEDED,
			null
		);
	}

	@Nested
	@DisplayName("prepareRefund")
	class PrepareRefund {

		@Test
		@DisplayName("정상적인 환불 준비 시 Refund가 생성된다")
		void prepareRefund_success() {
			// given
			given(paymentRepository.findById(any(PaymentId.class)))
				.willReturn(Optional.of(succeededPayment));
			given(refundRepository.findByPaymentId(PAYMENT_ID))
				.willReturn(Optional.empty());
			given(reservationReader.getPerformanceDateTime(any(ReservationId.class)))
				.willReturn(PERFORMANCE_START);

			// when
			RefundPrepareResult result = refundTransactionService.prepareRefund(PAYMENT_ID);

			// then
			assertThat(result.alreadyRefunded()).isFalse();
			assertThat(result.payment()).isNotNull();
			assertThat(result.refund()).isNotNull();
			assertThat(result.refund().getStatus()).isEqualTo(RefundStatus.REQUESTED);
		}

		@Test
		@DisplayName("존재하지 않는 결제에 대한 환불 준비 시 예외가 발생한다")
		void prepareRefund_paymentNotFound_throwsException() {
			// given
			given(paymentRepository.findById(any(PaymentId.class)))
				.willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> refundTransactionService.prepareRefund(PAYMENT_ID))
				.isInstanceOf(PaymentNotFoundException.class);
		}

		@Test
		@DisplayName("이미 환불된 결제에 대한 준비 시 기존 환불을 반환한다")
		void prepareRefund_alreadyRefunded_returnsExisting() {
			// given
			Refund existingRefund = Refund.reconstruct(
				RefundId.generate(),
				Money.of(10_000),
				RefundStatus.APPROVED,
				RefundPolicyStatus.REFUND_FULL,
				LocalDateTime.now(),
				null
			);

			given(paymentRepository.findById(any(PaymentId.class)))
				.willReturn(Optional.of(succeededPayment));
			given(refundRepository.findByPaymentId(PAYMENT_ID))
				.willReturn(Optional.of(existingRefund));

			// when
			RefundPrepareResult result = refundTransactionService.prepareRefund(PAYMENT_ID);

			// then
			assertThat(result.alreadyRefunded()).isTrue();
			assertThat(result.refund()).isSameAs(existingRefund);
		}
	}

	@Nested
	@DisplayName("completeRefund")
	class CompleteRefund {

		@Test
		@DisplayName("환불 완료 시 승인 처리되고 저장된다")
		void completeRefund_success() {
			// given
			given(paymentRepository.findById(any(PaymentId.class)))
				.willReturn(Optional.of(succeededPayment));
			given(refundRepository.save(any(Refund.class), eq(PAYMENT_ID), eq(REASON)))
				.willAnswer(inv -> inv.getArgument(0));
			given(paymentRepository.save(any(Payment.class)))
				.willAnswer(inv -> inv.getArgument(0));

			Refund refund = Refund.issue(
				LocalDateTime.now(),
				PERFORMANCE_START,
				Money.of(10_000)
			);

			// when
			Refund result = refundTransactionService.completeRefund(PAYMENT_ID, refund, REASON);

			// then
			assertThat(result.getStatus()).isEqualTo(RefundStatus.APPROVED);
			then(refundRepository).should().save(any(Refund.class), eq(PAYMENT_ID), eq(REASON));
			then(paymentRepository).should().save(any(Payment.class));
		}
	}
}
