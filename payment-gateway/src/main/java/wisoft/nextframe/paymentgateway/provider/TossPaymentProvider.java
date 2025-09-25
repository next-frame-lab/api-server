package wisoft.nextframe.paymentgateway.provider;

import static wisoft.nextframe.paymentgateway.api.PaymentGatewayController.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Profile("prod")
public class TossPaymentProvider implements PaymentProvider {

	private final RestClient restClient;

	public TossPaymentProvider(RestClient.Builder builder, @Value("${toss.secret-key}") String secretKey) {
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
	public boolean supports(String providerName) {
		return "toss".equalsIgnoreCase(providerName);
	}

	@Override
	public ConfirmResponse confirm(ConfirmRequest request) {
		Map response = restClient.post()
			.uri("/v1/payments/confirm")
			.contentType(MediaType.APPLICATION_JSON)
			.body(Map.of(
				"paymentKey", request.paymentKey(),
				"orderId", request.orderId(),
				"amount", request.amount())
			)
			.retrieve()
			.body(Map.class);

		String status = (String)response.getOrDefault("status", "");
		if ("DONE".equals(status)) {
			int totalAmount = (int)response.getOrDefault("totalAmount", request.amount());
			return new ConfirmResponse(true, totalAmount, null, null);
		}
		String code = (String)response.getOrDefault("code", "TOSS_ERROR");
		String msg = (String)response.getOrDefault("message", "토스 승인 실패");
		return new ConfirmResponse(false, 0, code, msg);
	}
}
