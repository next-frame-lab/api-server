package wisoft.nextframe.payment.application.payment;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.payment.application.payment.port.output.PaymentGateway;
import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.payment.presentation.payment.dto.PaymentConfirmRequest;

// 유스케이스 오케스트레이션 담당
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

	private final PaymentGateway paymentGateway;
	private final PaymentTransactionService paymentTransactionService;

	public Payment confirmPayment(PaymentConfirmRequest request) {

		// 트랜잭션 없이 외부 호출
		PaymentGateway.PaymentConfirmResult result = paymentGateway.confirmPayment(
			request.paymentKey(),
			request.orderId(),
			request.amount()
		);

		return paymentTransactionService.applyConfirmResult(request, result);
	}
}