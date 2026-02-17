package wisoft.nextframe.payment.application.payment;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import wisoft.nextframe.payment.application.payment.port.output.PaymentGateway;
import wisoft.nextframe.payment.application.payment.port.output.PaymentRepository;
import wisoft.nextframe.payment.application.payment.port.output.ReservationReader;
import wisoft.nextframe.payment.application.payment.port.output.TicketIssueResult;
import wisoft.nextframe.payment.application.payment.port.output.TicketingClient;
import wisoft.nextframe.payment.domain.ReservationId;
import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.payment.domain.payment.PaymentStatus;
import wisoft.nextframe.payment.domain.payment.exception.PaymentConfirmedFailedException;
import wisoft.nextframe.payment.infra.config.AbstractIntegrationTest;
import wisoft.nextframe.payment.presentation.payment.dto.PaymentConfirmRequest;

public class PaymentServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private PaymentRepository paymentRepository;

	@MockitoBean
	private TicketingClient ticketingClient;

	@MockitoBean
	private PaymentGateway paymentGateway;

	@MockitoBean
	private ReservationReader reservationReader;

	@BeforeEach
	void setUp() {
		given(reservationReader.exists(any(ReservationId.class))).willReturn(true);
	}

	@Test
	@DisplayName("결제 승인 성공 시 Payment가 승인 상태로 저장된다")
	void confirmPaymentSuccess() {
		//given
		given(paymentGateway.confirmPayment(anyString(), anyString(), anyInt()))
			.willReturn(new PaymentGateway.PaymentConfirmResult(
				true,   // success
				10000,  // totalAmount
				null,   // errorCode
				null    // errorMessage
			));

		given(ticketingClient.issueTicket(any(ReservationId.class)))
			.willReturn(new TicketIssueResult(UUID.randomUUID()));

		PaymentConfirmRequest request = new PaymentConfirmRequest(
			"test_payment_key",
			UUID.randomUUID().toString(),
			10000
		);

		//when
		Payment payment = paymentService.confirmPayment(request);

		//then
		assertThat(payment.isSucceeded()).isTrue();
		assertThat(paymentRepository.findById(payment.getId())).isPresent();
	}

	@Test
	@DisplayName("티켓 발급 실패 시 PG 취소 후 FAILED 상태로 저장된다")
	void failPaymentWhenTicketIssueFails() {
		// given: PG 승인 성공
		given(paymentGateway.confirmPayment(anyString(), anyString(), anyInt()))
			.willReturn(new PaymentGateway.PaymentConfirmResult(
				true,
				10000,
				null,
				null
			));

		// given: 티켓 발급 호출 실패
		given(ticketingClient.issueTicket(any(ReservationId.class)))
			.willThrow(new RuntimeException("ticket server down"));

		// given: PG 취소 성공
		given(paymentGateway.cancelPayment(anyString(), anyInt(), anyString()))
			.willReturn(new PaymentGateway.PaymentCancelResult(
				true, 10000, "txn-key", null, null
			));

		UUID reservationId = UUID.randomUUID();
		PaymentConfirmRequest request = new PaymentConfirmRequest(
			"test_payment_key",
			reservationId.toString(),
			10000
		);

		// when & then: 티켓 발급 실패로 인해 PaymentConfirmedFailedException 발생
		assertThatThrownBy(() -> paymentService.confirmPayment(request))
			.isInstanceOf(PaymentConfirmedFailedException.class);

		// then: 티켓 발급이 호출되었는지 확인
		then(ticketingClient).should().issueTicket(eq(ReservationId.of(reservationId)));

		// then: PG 취소가 호출되었는지 확인
		then(paymentGateway).should().cancelPayment(eq(reservationId.toString()), eq(10000), eq("티켓 발급 실패"));

		// then: Payment가 FAILED 상태로 DB에 저장됨
		var saved = paymentRepository.findByReservationId(ReservationId.of(reservationId));
		assertThat(saved).isPresent();
		assertThat(saved.get().getStatus()).isEqualTo(PaymentStatus.FAILED);
	}
}
