package wisoft.nextframe.payment.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wisoft.nextframe.common.Money;
import wisoft.nextframe.payment.application.payment.port.output.ExternalPaymentClient;
import wisoft.nextframe.payment.application.payment.port.output.PaymentRepository;
import wisoft.nextframe.payment.domain.fixture.TestPaymentFactory;
import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.payment.domain.payment.PaymentId;
import wisoft.nextframe.payment.domain.payment.PaymentStatus;
import wisoft.nextframe.payment.domain.payment.exception.PaymentNotFoundException;
import wisoft.nextframe.reservation.domain.ReservationId;
import wisoft.nextframe.ticketing.application.TicketService;

public class PaymentServiceTest {

	private PaymentRepository paymentRepository;
	private ExternalPaymentClient externalPaymentClient;
	private TicketService ticketService;

	private PaymentService paymentService;

	@BeforeEach
	void setUp() {
		paymentRepository = mock(PaymentRepository.class);
		externalPaymentClient = mock(ExternalPaymentClient.class);
		ticketService = mock(TicketService.class);
		paymentService = new PaymentService(
			paymentRepository,
			externalPaymentClient,
			ticketService
		);
	}

	@Test
	@DisplayName("결제 요청이 오면 상태는 REQUESTED가 되고 외부 결제 요청을 보낸다.")
	void requestPayment_success() {
		// given
		ReservationId reservationId = ReservationId.of(UUID.randomUUID());
		Money amount = Money.of(50000);

		// when
		Payment payment = paymentService.requestPayment(reservationId, amount, LocalDateTime.now());

		// then
		verify(externalPaymentClient).requestPayment(any());
		verify(paymentRepository).save(payment);
		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REQUESTED);
	}

	@Test
	@DisplayName("결제 승인 시 상태는 SUCCEEDED로 변경된다")
	void approvePayment_success() {
		// given
		Payment payment = TestPaymentFactory.requested();
		UUID seatId = UUID.randomUUID();
		UUID scheduleId = UUID.randomUUID();

		when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

		// when
		paymentService.approvePayment(payment.getId());

		// then
		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
		verify(paymentRepository).save(payment);
	}

	@Test
	@DisplayName("결제 승인 시 결제가 존재하지 않으면 PaymentNotFoundException이 발생한다")
	void approvePayment_notFound() {
		when(paymentRepository.findById(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> paymentService.approvePayment(PaymentId.of(UUID.randomUUID())))
			.isInstanceOf(PaymentNotFoundException.class);

		verify(paymentRepository, never()).save(any());
	}

	@Test
	@DisplayName("결제 실패 시 상태는 FAILED로 변경된다")
	void failPayment_changesStatusToFailed() {
		// given
		Payment payment = TestPaymentFactory.requested();
		when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

		// when
		paymentService.failPayment(payment.getId());

		// then
		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
		verify(paymentRepository).save(payment);
	}

	@Test
	@DisplayName("결제 실패 시 결제가 존재하지 않으면 PaymentNotFoundException이 발생한다")
	void failPayment_notFound() {
		when(paymentRepository.findById(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> paymentService.failPayment(PaymentId.of(UUID.randomUUID())))
			.isInstanceOf(PaymentNotFoundException.class);

		verify(paymentRepository, never()).save(any());
	}

	@Test
	@DisplayName("결제 승인 시 Ticketing BC에 티켓 발급 요청을 보낸다")
	void approvePayment_shouldCallTicketingClient() {
		// given
		Payment payment = TestPaymentFactory.requested();

		UUID seatId = UUID.fromString("33333333-3333-3333-3333-333333333333");
		UUID scheduleId = UUID.fromString("44444444-4444-4444-4444-444444444444");

		when(paymentRepository.findById(payment.getId()))
			.thenReturn(Optional.of(payment));

		// when
		paymentService.approvePayment(payment.getId());

	}

}


