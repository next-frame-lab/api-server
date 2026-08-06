package wisoft.nextframe.payment.application.refund;

import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.payment.application.payment.exception.PaymentGatewayExternalCallFailedException;
import wisoft.nextframe.payment.application.payment.exception.PaymentGatewayTemporarilyUnavailableException;
import wisoft.nextframe.payment.application.payment.outbox.cancel.ReservationCancelOutboxService;
import wisoft.nextframe.payment.application.payment.port.output.PaymentGateway;
import wisoft.nextframe.payment.application.refund.RefundTransactionService.RefundPrepareResult;
import wisoft.nextframe.payment.domain.refund.Refund;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService {

	private final PaymentGateway paymentGateway;
	private final RefundTransactionService refundTransactionService;
	private final ReservationCancelOutboxService reservationCancelOutboxService;

	public Refund refund(UUID paymentId, String reason) {
		// 1. 환불 준비 (검증 + Refund 생성, 트랜잭션)
		RefundPrepareResult prepareResult = refundTransactionService.prepareRefund(paymentId);

		if (prepareResult.alreadyRefunded()) {
			return prepareResult.refund();
		}

		Refund refund = prepareResult.refund();
		String paymentKey = prepareResult.payment().getPaymentKey();
		String orderId = prepareResult.payment().getReservationId().value().toString();
		int cancelAmount = refund.getRefundedAmount().getValue().intValue();

		// 2. PG 호출 전 Refund를 REQUESTED로 선저장 (실패 시 재조회 근거 확보)
		refundTransactionService.saveRequested(paymentId, refund, reason);

		// 3. PG 환불 요청 (트랜잭션 없이 외부 호출)
		PaymentGateway.PaymentCancelResult cancelResult;
		try {
			cancelResult = paymentGateway.cancelPayment(paymentKey, orderId, cancelAmount, reason);
		} catch (PaymentGatewayTemporarilyUnavailableException | PaymentGatewayExternalCallFailedException e) {
			log.error("PG 환불 호출 실패, Refund는 REQUESTED로 남아 재시도 대상 - paymentId: {}, refundId: {}",
				paymentId, refund.getRefundId().getValue());
			throw e;
		}

		if (!cancelResult.isSuccess()) {
			log.error("PG 환불 실패 - paymentId: {}, errorCode: {}, errorMessage: {}",
				paymentId, cancelResult.errorCode(), cancelResult.errorMessage());
			throw new RefundCancelFailedException(cancelResult.errorCode());
		}

		// 4. 환불 완료 저장 (트랜잭션)
		Refund completed = refundTransactionService.completeRefund(paymentId, refund, reason);

		// 5. 예약 취소 + 좌석 해제 (outbox 패턴으로 신뢰성 보장)
		reservationCancelOutboxService.cancelOrEnqueue(
			paymentId,
			prepareResult.payment().getReservationId().value()
		);

		return completed;
	}
}
