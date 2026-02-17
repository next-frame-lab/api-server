package wisoft.nextframe.payment.application.payment;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.payment.application.payment.port.output.PaymentGateway;
import wisoft.nextframe.payment.application.payment.port.output.TicketingClient;
import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.payment.presentation.payment.dto.PaymentConfirmRequest;

// 유스케이스 오케스트레이션 담당
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

	private final PaymentGateway paymentGateway;
	private final TicketingClient ticketingClient;
	private final PaymentTransactionService paymentTransactionService;

	public Payment confirmPayment(PaymentConfirmRequest request) {

		// 1. 트랜잭션 없이 외부 PG 호출
		PaymentGateway.PaymentConfirmResult result = paymentGateway.confirmPayment(
			request.paymentKey(),
			request.orderId(),
			request.amount()
		);

		// PG 실패 시 바로 FAILED 저장
		if (!result.isSuccess()) {
			return paymentTransactionService.applyConfirmResult(request, result);
		}

		// 2. PG 성공 → 티켓 발급 동기 호출
		try {
			ticketingClient.issueTicket(
				wisoft.nextframe.payment.domain.ReservationId.of(
					java.util.UUID.fromString(request.orderId())
				)
			);
		} catch (Exception e) {
			log.error("티켓 발급 실패 - orderId={}, error={}", request.orderId(), e.getMessage());

			// best-effort PG 취소
			try {
				paymentGateway.cancelPayment(request.orderId(), request.amount(), "티켓 발급 실패");
			} catch (Exception cancelEx) {
				log.error("PG 취소도 실패 - orderId={}, error={} (수동 확인 필요)", request.orderId(), cancelEx.getMessage());
			}

			// FAILED 결과로 저장
			PaymentGateway.PaymentConfirmResult failedResult = new PaymentGateway.PaymentConfirmResult(
				false, result.totalAmount(), "TICKET_ISSUE_FAILED", e.getMessage()
			);
			return paymentTransactionService.applyConfirmResult(request, failedResult);
		}

		// 3. 티켓 발급 성공 → Payment SUCCEEDED 저장
		return paymentTransactionService.applyConfirmResult(request, result);
	}
}
