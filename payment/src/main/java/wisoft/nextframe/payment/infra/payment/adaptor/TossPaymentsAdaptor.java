package wisoft.nextframe.payment.infra.payment.adaptor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import wisoft.nextframe.payment.application.payment.port.output.TossPaymentsClient;

@Component
@Profile("prod")
public class TossPaymentsAdaptor implements TossPaymentsClient {

	private final RestClient restClient;

	public TossPaymentsAdaptor(RestClient.Builder builder, @Value("${toss.secret-key}") String secretKey) {
		if (secretKey == null || secretKey.isEmpty()) {
			throw new IllegalStateException("toss.secret-key가 설정되어 있지 않습니다.");
		}
		String encoded = Base64.getEncoder()
			.encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

		this.restClient = builder
			.baseUrl("https://api.tosspayments.com")
			.defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoded)
			.build();
	}

	@Override
	public Map<String, Object> confirmPayment(String paymentKey, String orderId, int amount) {
		return restClient.post()
			.uri("/v1/payments/confirm")
			.contentType(MediaType.APPLICATION_JSON)
			.body(Map.of(
				"paymentKey", paymentKey,
				"orderId", orderId,
				"amount", amount)
			)
			.retrieve()
			.body(Map.class);
	}
}
