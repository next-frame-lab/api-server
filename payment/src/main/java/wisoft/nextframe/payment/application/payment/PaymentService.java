package wisoft.nextframe.payment.application.payment;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.payment.application.payment.exception.PaymentGatewayExternalCallFailedException;
import wisoft.nextframe.payment.application.payment.exception.PaymentGatewayTemporarilyUnavailableException;
import wisoft.nextframe.payment.application.payment.exception.ReservationExpiredException;
import wisoft.nextframe.payment.application.payment.port.output.PaymentGateway;
import wisoft.nextframe.payment.application.payment.port.output.ReservationReader;
import wisoft.nextframe.payment.domain.ReservationId;
import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.payment.presentation.payment.dto.PaymentConfirmRequest;

// 유스케이스 오케스트레이션 담당
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

	private final PaymentGateway paymentGateway;
	private final ReservationReader reservationReader;
	private final PaymentTransactionService paymentTransactionService;

	public Payment confirmPayment(PaymentConfirmRequest request) {

		// 1. PG 호출 전 예약 유효성 검증 (만료·취소된 예약 차단)
		ReservationId reservationId = ReservationId.of(java.util.UUID.fromString(request.orderId()));
		if (!reservationReader.isPayable(reservationId)) {
			throw new ReservationExpiredException(reservationId.value());
		}

		// 2. PG 호출 전 Payment를 REQUESTED로 선저장 (실패 시 재조회 근거 확보)
		Payment payment = paymentTransactionService.createRequested(reservationId, request.amount());

		// 3. 트랜잭션 없이 외부 PG 호출
		PaymentGateway.PaymentConfirmResult result;
		try {
			result = paymentGateway.confirmPayment(
				request.paymentKey(),
				request.orderId(),
				request.amount()
			);
		} catch (PaymentGatewayTemporarilyUnavailableException | PaymentGatewayExternalCallFailedException e) {
			paymentTransactionService.handlePaymentFailure(payment);
			throw e;
		}

		// 4. 결과 영속화 + 도메인 이벤트 발행 (outbox 패턴으로 비동기 처리)
		return paymentTransactionService.applyConfirmResult(request, result);
	}
}