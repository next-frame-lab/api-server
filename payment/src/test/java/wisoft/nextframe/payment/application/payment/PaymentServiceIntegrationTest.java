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
import wisoft.nextframe.payment.application.payment.exception.ReservationExpiredException;
import wisoft.nextframe.payment.application.payment.port.output.TicketIssueResult;
import wisoft.nextframe.payment.application.payment.port.output.TicketingClient;
import wisoft.nextframe.payment.domain.ReservationId;
import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.payment.domain.payment.PaymentStatus;
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
		given(reservationReader.isPayable(any(ReservationId.class))).willReturn(true);
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
	@DisplayName("만료된 예약으로 결제 시도 시 PG 호출 없이 ReservationExpiredException이 발생한다")
	void confirmPayment_expiredReservation_throwsBeforePg() {
		// given: 만료된 예약
		given(reservationReader.isPayable(any(ReservationId.class))).willReturn(false);

		PaymentConfirmRequest request = new PaymentConfirmRequest(
			"test_payment_key",
			UUID.randomUUID().toString(),
			10000
		);

		// when & then
		assertThatThrownBy(() -> paymentService.confirmPayment(request))
			.isInstanceOf(ReservationExpiredException.class);

		// then: PG는 호출되지 않아야 함
		then(paymentGateway).should(never()).confirmPayment(anyString(), anyString(), anyInt());
	}

	@Test
	@DisplayName("PG 승인 성공 시 Payment가 SUCCEEDED 상태로 저장되고 PG 취소는 발생하지 않는다")
	void confirmPayment_pgSuccess_savedAsSucceeded() {
		// given: PG 승인 성공
		given(paymentGateway.confirmPayment(anyString(), anyString(), anyInt()))
			.willReturn(new PaymentGateway.PaymentConfirmResult(
				true,
				10000,
				null,
				null
			));

		UUID reservationId = UUID.randomUUID();
		PaymentConfirmRequest request = new PaymentConfirmRequest(
			"test_payment_key",
			reservationId.toString(),
			10000
		);

		// when
		Payment payment = paymentService.confirmPayment(request);

		// then: Payment가 SUCCEEDED 상태로 저장됨
		assertThat(payment.isSucceeded()).isTrue();
		var saved = paymentRepository.findByReservationId(ReservationId.of(reservationId));
		assertThat(saved).isPresent();
		assertThat(saved.get().getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);

		// then: 아웃박스 패턴으로 전환되었으므로 PG 취소는 호출되지 않음
		then(paymentGateway).should(never()).cancelPayment(anyString(), anyString(), anyInt(), anyString());
	}
}
