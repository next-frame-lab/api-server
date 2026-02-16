package wisoft.nextframe.payment.infra.payment.adapter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.payment.application.payment.exception.PaymentGatewayExternalCallFailedException;
import wisoft.nextframe.payment.application.payment.exception.PaymentGatewayTemporarilyUnavailableException;
import wisoft.nextframe.payment.application.payment.port.output.PaymentGateway;

@Slf4j
@Component
@Profile("prod")
public class HttpPaymentGatewayAdapter implements PaymentGateway {

	private final RestClient restClient;

	public HttpPaymentGatewayAdapter(RestClient.Builder builder, @Value("${payment-gateway.url}") String baseUrl) {
		var factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(3_000);
		factory.setReadTimeout(5_000);

		this.restClient = builder
			.baseUrl(baseUrl)
			.requestFactory(factory)
			.build();
	}

	@Override
	@CircuitBreaker(name = "paymentGateway", fallbackMethod = "confirmPaymentFallback")
	public PaymentConfirmResult confirmPayment(String paymentKey, String orderId, int amount) {
		String raw = restClient.post()
			.uri("/payments/confirm?provider=toss")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.body(new ConfirmRequest(paymentKey, orderId, amount))
			.retrieve()
			.body(String.class);

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

	@Override
	@CircuitBreaker(name = "paymentGateway", fallbackMethod = "cancelPaymentFallback")
	public PaymentCancelResult cancelPayment(String orderId, int cancelAmount, String cancelReason) {
		String raw = restClient.post()
			.uri("/payments/cancel?provider=toss")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.body(new CancelRequest(orderId, cancelAmount, cancelReason))
			.retrieve()
			.body(String.class);

		log.info("gateway cancel raw response = {}", raw);

		try {
			ObjectMapper mapper = new ObjectMapper();
			CancelResponse response = mapper.readValue(raw, CancelResponse.class);

			return new PaymentCancelResult(
				response.isSuccess(),
				response.cancelAmount(),
				response.transactionKey(),
				response.errorCode(),
				response.errorMessage()
			);
		} catch (Exception e) {
			log.error("Failed to parse cancel response: {}", raw, e);
			return new PaymentCancelResult(false, 0, null, "PARSE_ERROR", raw);
		}
	}

	private PaymentConfirmResult confirmPaymentFallback(String paymentKey, String orderId, int amount, Throwable e) {
		if (e instanceof CallNotPermittedException) {
			log.warn("결제 승인 차단됨 [CIRCUIT_BREAKER_OPEN]. orderId={}", orderId);
			throw new PaymentGatewayTemporarilyUnavailableException("confirm", e);
		}

		log.warn("결제 승인 외부 호출 실패 [PAYMENT_GATEWAY_EXTERNAL_CALL_FAILED]. orderId={}, error={}",
			orderId, e.toString());
		throw new PaymentGatewayExternalCallFailedException("confirm", e);
	}

	private PaymentCancelResult cancelPaymentFallback(String orderId, int cancelAmount, String cancelReason,
		Throwable e) {
		if (e instanceof CallNotPermittedException) {
			log.warn("결제 취소 차단됨 [CIRCUIT_BREAKER_OPEN]. orderId={}", orderId);
			throw new PaymentGatewayTemporarilyUnavailableException("cancel", e);
		}

		log.warn("결제 취소 외부 호출 실패 [PAYMENT_GATEWAY_EXTERNAL_CALL_FAILED]. orderId={}, error={}",
			orderId, e.toString());
		throw new PaymentGatewayExternalCallFailedException("cancel", e);
	}

	public record ConfirmRequest(String paymentKey, String orderId, int amount) {
	}

	public record ConfirmResponse(boolean isSuccess, int totalAmount, String errorCode, String errorMessage) {
	}

	public record CancelRequest(String orderId, int cancelAmount, String cancelReason) {
	}

	public record CancelResponse(
		boolean isSuccess,
		int cancelAmount,
		String transactionKey,
		String errorCode,
		String errorMessage) {
	}
}