package wisoft.nextframe.payment.infra.payment.adaptor;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import wisoft.nextframe.payment.application.payment.port.output.PaymentClient;

@SpringBootTest
class TossPaymentsStubTest {

	@Autowired
	private PaymentClient paymentClient;

	@Test
	void stubReturnsAlwaysSuccess() {
		var result =
			paymentClient.confirmPayment("dummy-key", "order-123", 10000);

		assertThat(result.get("status")).isEqualTo("DONE");
		assertThat(result.get("paymentKey")).isEqualTo("dummy-key");
		assertThat(result.get("orderId")).isEqualTo("order-123");
	}
}
