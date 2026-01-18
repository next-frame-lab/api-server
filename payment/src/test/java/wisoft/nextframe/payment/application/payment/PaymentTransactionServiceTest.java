package wisoft.nextframe.payment.application.payment;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import wisoft.nextframe.payment.application.payment.exception.ReservationNotFoundException;
import wisoft.nextframe.payment.application.payment.port.output.PaymentGateway;
import wisoft.nextframe.payment.application.payment.port.output.PaymentRepository;
import wisoft.nextframe.payment.application.payment.port.output.ReservationReader;
import wisoft.nextframe.payment.common.Money;
import wisoft.nextframe.payment.domain.ReservationId;
import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.payment.domain.payment.exception.PaymentConfirmedFailedException;
import wisoft.nextframe.payment.presentation.payment.dto.PaymentConfirmRequest;

@ExtendWith(MockitoExtension.class)
class PaymentTransactionServiceTest {

	@Mock
	PaymentRepository paymentRepository;

	@Mock
	ReservationReader reservationReader;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	@InjectMocks
	PaymentTransactionService paymentTransactionService;

	@BeforeEach
	void setUp() {
		lenient().when(reservationReader.exists(any())).thenReturn(true);

		lenient().when(paymentRepository.save(any(Payment.class)))
			.thenAnswer(inv -> inv.getArgument(0));
	}

	@Test
	@DisplayName("PG 승인 성공 시 결제를 승인 상태로 저장한다")
	void applyConfirmResult_success_createsAndSavesApprovedPayment() {
		// given
		UUID reservationId = UUID.randomUUID();
		PaymentConfirmRequest request = new PaymentConfirmRequest(
			"paymentKey",
			reservationId.toString(),
			10_000
		);

		PaymentGateway.PaymentConfirmResult result = new PaymentGateway.PaymentConfirmResult(
			true,
			10_000,
			null,
			null
		);

		Payment payment = Payment.request(
			Money.of(10_000),
			ReservationId.of(reservationId),
			LocalDateTime.now()
		);

		given(paymentRepository.findByReservationId(any())).willReturn(Optional.of(payment));
		// save()는 보통 저장된 엔티티를 반환하니 그대로 반환하도록 세팅
		given(paymentRepository.save(any(Payment.class))).willAnswer(inv -> inv.getArgument(0));

		// when
		Payment saved = paymentTransactionService.applyConfirmResult(request, result);

		// then
		assertThat(saved.isSucceeded()).isTrue();
		assertThat(saved.getReservationId().value()).isEqualTo(reservationId);
		then(paymentRepository).should(times(1)).save(any(Payment.class));
	}

	@Test
	@DisplayName("PG 승인 실패 응답이면 결제를 실패로 저장하고 예외를 던진다")
	void shouldSaveFailedPaymentAndThrow_whenGatewayResultIsFailure() {
		// given
		UUID reservationUuid = UUID.randomUUID();
		PaymentConfirmRequest request = new PaymentConfirmRequest(
			"paymentKey",
			reservationUuid.toString(),
			10_000
		);

		PaymentGateway.PaymentConfirmResult result =
			new PaymentGateway.PaymentConfirmResult(false, 10_000, "ERR", "fail");

		given(paymentRepository.findByReservationId(any()))
			.willReturn(Optional.empty());

		given(paymentRepository.save(any(Payment.class)))
			.willAnswer(inv -> inv.getArgument(0));

		// when + then
		assertThatThrownBy(() -> paymentTransactionService.applyConfirmResult(request, result))
			.isInstanceOf(PaymentConfirmedFailedException.class);

		then(paymentRepository).should(times(1)).save(any(Payment.class));
	}

	@Test
	@DisplayName("이미 승인된 결제가 존재하면 새로운 결제를 생성하지 않고 기존 결제를 반환한다")
	void applyConfirmResult_whenExistingSucceededPayment_returnsExisting_andDoesNotSave() {
		// given
		UUID reservationUuid = UUID.randomUUID();
		PaymentConfirmRequest request = new PaymentConfirmRequest(
			"paymentKey",
			reservationUuid.toString(),
			10_000
		);

		PaymentGateway.PaymentConfirmResult result =
			new PaymentGateway.PaymentConfirmResult(true, 10_000, null, null);

		// 이미 성공한 Payment를 준비 (프로젝트에 맞게 fixture/factory 써도 됨)
		Payment existing = Payment.request(
			Money.of(10_000),
			ReservationId.of(reservationUuid),
			LocalDateTime.now()
		);
		existing.approve(); // succeeded 상태로 변경

		given(paymentRepository.findByReservationId(any())).willReturn(Optional.of(existing));
		// when
		Payment returned = paymentTransactionService.applyConfirmResult(request, result);

		// then
		assertThat(returned).isSameAs(existing);
		then(paymentRepository).should(never()).save(any(Payment.class));
	}

	@Test
	@DisplayName("예약 정보가 존재하지 않으면 ReservationNotFoundException이 발생한다")
	void applyConfirmResult_ThrowsException_WhenReservationNotFound() {
		// given
		String orderId = UUID.randomUUID().toString();
		ReservationId reservationId = ReservationId.of(UUID.fromString(orderId));
		PaymentConfirmRequest request = new PaymentConfirmRequest("paymentKey", orderId, 10_000);

		// reservationReader.exists가 false를 반환하도록 설정
		given(reservationReader.exists(reservationId)).willReturn(false);

		// when & then
		assertThrows(ReservationNotFoundException.class, () -> {
			paymentTransactionService.applyConfirmResult(request, any());
		});
	}

}