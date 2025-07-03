package wisoft.nextframe.payment.infra.payment.adaptor;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import wisoft.nextframe.payment.application.payment.port.output.PaymentClient;

@Component
@Profile({"loadtest", "dev"})
public class StubPaymentAdaptor implements PaymentClient {

	@Override
	public Map<String, Object> confirmPayment(String paymentKey, String orderId, int amount) {
		// 항상 성공하는 응답을 반환
		return Map.of(
			"status", "DONE",
			"paymentKey", paymentKey,
			"orderId", orderId,
			"approvedAt", LocalDateTime.now().toString()
		);
	}
}