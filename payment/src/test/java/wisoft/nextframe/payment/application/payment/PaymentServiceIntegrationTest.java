package wisoft.nextframe.payment.application.payment;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import wisoft.nextframe.payment.application.payment.port.output.PaymentGateway;
import wisoft.nextframe.payment.application.payment.port.output.PaymentRepository;
import wisoft.nextframe.payment.application.payment.port.output.ReservationReader;
import wisoft.nextframe.payment.application.payment.port.output.TicketingClient;
import wisoft.nextframe.payment.domain.ReservationId;
import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.payment.infra.config.AbstractIntegrationTest;
import wisoft.nextframe.payment.infra.ticketissue.adapter.JpaTicketIssueOutboxRepository;
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

	// outbox DB 상태를 직접 확인하기 위한 용도
	@Autowired
	private JpaTicketIssueOutboxRepository jpaOutboxRepository;

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

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	@Test
	@DisplayName("티켓 서버 호출이 실패하면 outbox에 PENDING이 적재된다.")
	void enqueueOutboxWhenTicketIssueFails() {
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

		UUID reservationId = UUID.randomUUID();
		PaymentConfirmRequest request = new PaymentConfirmRequest(
			"test_payment_key",
			reservationId.toString(),
			10000
		);

		// when
		paymentService.confirmPayment(request);

		// 1) 여기서 먼저 확인: 핸들러가 티켓 호출을 실제로 했는지
		then(ticketingClient).should(times(1)).issueTicket(any(ReservationId.class));
		then(ticketingClient).should().issueTicket(eq(ReservationId.of(reservationId)));

		// 2) outbox가 정말 쌓였는지 확인 (없으면 원인 좁혀짐)
		var all = jpaOutboxRepository.findAll();
		assertThat(all).isNotEmpty();

		var outbox = jpaOutboxRepository.findByReservationId(reservationId).orElseThrow();
		assertThat(outbox.getStatus()).isEqualTo("PENDING");
	}
}
