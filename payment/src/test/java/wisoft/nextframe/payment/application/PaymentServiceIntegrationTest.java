package wisoft.nextframe.payment.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import wisoft.nextframe.payment.application.payment.PaymentService;
import wisoft.nextframe.payment.application.payment.port.output.PaymentGateway;
import wisoft.nextframe.payment.application.payment.port.output.PaymentRepository;
import wisoft.nextframe.payment.application.payment.port.output.TicketingClient;
import wisoft.nextframe.payment.domain.ReservationId;
import wisoft.nextframe.payment.domain.payment.Payment;
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

	@Test
	@DisplayName("결제 승인 성공 시 Payment 저장 및 티켓 발급 요청이 발생한다.")
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
		verify(ticketingClient, times(1)).issueTicket(any(ReservationId.class));

	}
}
