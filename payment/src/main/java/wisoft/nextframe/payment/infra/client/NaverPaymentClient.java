package wisoft.nextframe.payment.infra.client;

import org.springframework.stereotype.Component;

import wisoft.nextframe.payment.application.port.output.ExternalPaymentClient;
import wisoft.nextframe.payment.domain.Payment;

@Component
public class NaverPaymentClient implements ExternalPaymentClient {

	@Override
	public boolean requestPayment(Payment payment) {
		// TODO: 실제 외부 결제 API 호출 로직 구현
		return true; // 성공 가정
	}
}
