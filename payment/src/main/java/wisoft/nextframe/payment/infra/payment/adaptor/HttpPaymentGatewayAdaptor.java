package wisoft.nextframe.payment.infra.payment.adaptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.payment.application.payment.port.output.PaymentGateway;

@Slf4j
@Component
@Profile("prod")
public class HttpPaymentGatewayAdaptor implements PaymentGateway {

	private final RestClient restClient;

	public HttpPaymentGatewayAdaptor(RestClient.Builder builder, @Value("${payment-gateway.url}") String baseUrl) {
		this.restClient = builder
			.baseUrl(baseUrl)
			.build();
	}

	@Override
	public PaymentConfirmResult confirmPayment(String paymentKey, String orderId, int amount) {
		String raw = restClient.post()
			.uri("/payments/confirm?provider=toss")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.body(new ConfirmRequest(paymentKey, orderId, amount))
			.retrieve()
			.body(String.class); // 👈 응답을 무조건 String으로 받기

		log.info("gateway raw response = {}", raw);

		// JSON 정상일 때만 매핑 시도
		try {
			ObjectMapper mapper = new ObjectMapper();
			ConfirmResponse response = mapper.readValue(raw, ConfirmResponse.class);

			return new PaymentConfirmResult(
				response.isSuccess(),
				response.totalAmount(),
				response.errorCode(),
				response.errorMessage()
			);
		} catch (Exception e) {
			log.error("Failed to parse response: {}", raw, e);
			return new PaymentConfirmResult(false, 0, "PARSE_ERROR", raw);
		}
	}

	public record ConfirmRequest(String paymentKey, String orderId, int amount) {
	}

	public record ConfirmResponse(boolean isSuccess, int totalAmount, String errorCode, String errorMessage) {
	}
}
