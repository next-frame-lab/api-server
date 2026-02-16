package wisoft.nextframe.paymentgateway.provider;

import static wisoft.nextframe.paymentgateway.api.PaymentGatewayController.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("prod")
public class TossPaymentProvider implements PaymentProvider {

	private final RestClient restClient;
	private final ConcurrentHashMap<String, String> paymentKeyStore = new ConcurrentHashMap<>();

	public TossPaymentProvider(
		RestClient.Builder builder,
		@Value("${toss.secret-key}") String secretKey,
		@Value("${toss.base-url:https://api.tosspayments.com}") String baseUrl
	) {
		if (secretKey == null || secretKey.isEmpty()) {
			throw new IllegalStateException("toss.secret-key가 설정되어 있지 않습니다.");
		}
		String encoded = Base64.getEncoder()
			.encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

		var factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(2_000);
		factory.setReadTimeout(3_000);

		this.restClient = builder
			.baseUrl(baseUrl)
			.defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoded)
			.requestFactory(factory)
			.build();
	}

	@Override
	public boolean supports(String providerName) {
		return "toss".equalsIgnoreCase(providerName);
	}

	@Override
	@CircuitBreaker(name = "tossConfirm", fallbackMethod = "confirmFallback")
	@Bulkhead(name = "tossConfirm")
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
			paymentKeyStore.put(request.orderId(), request.paymentKey());
			int totalAmount = (int)response.getOrDefault("totalAmount", request.amount());
			return new ConfirmResponse(true, totalAmount, null, null);
		}
		String code = (String)response.getOrDefault("code", "TOSS_ERROR");
		String msg = (String)response.getOrDefault("message", "토스 승인 실패");
		return new ConfirmResponse(false, 0, code, msg);
	}

	private ConfirmResponse confirmFallback(ConfirmRequest request, CallNotPermittedException e) {
		log.warn("Toss 결제 승인 차단됨 [CIRCUIT_BREAKER_OPEN]");
		return new ConfirmResponse(false, 0, "PG_TEMPORARY_UNAVAILABLE", "결제 서비스가 일시적으로 불안정합니다.");
	}

	private ConfirmResponse confirmFallback(ConfirmRequest request, BulkheadFullException e) {
		log.warn("Toss 결제 승인 차단됨 [BULKHEAD_FULL]");
		return new ConfirmResponse(false, 0, "PG_TEMPORARY_UNAVAILABLE", "결제 요청이 집중되고 있습니다.");
	}

	private ConfirmResponse confirmFallback(ConfirmRequest request, Throwable e) {
		log.warn("Toss 결제 승인 실패: {}", e.toString());
		return new ConfirmResponse(false, 0, "PG_NETWORK_ERROR", "PG 통신 중 오류가 발생했습니다.");
	}

	@Override
	public CancelResponse cancel(CancelRequest request) {
		String paymentKey = paymentKeyStore.get(request.orderId());
		if (paymentKey == null) {
			return new CancelResponse(false, 0, null, "PAYMENT_KEY_NOT_FOUND",
				"결제 키를 찾을 수 없습니다. orderId=" + request.orderId());
		}

		try {
			Map response = restClient.post()
				.uri("/v1/payments/{paymentKey}/cancel", paymentKey)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of(
					"cancelReason", request.cancelReason(),
					"cancelAmount", request.cancelAmount())
				)
				.retrieve()
				.body(Map.class);

			String status = (String)response.getOrDefault("status", "");
			if ("CANCELED".equals(status) || "PARTIAL_CANCELED".equals(status)) {
				String transactionKey = (String)response.getOrDefault("transactionKey", "");
				return new CancelResponse(true, request.cancelAmount(), transactionKey, null, null);
			}

			String code = (String)response.getOrDefault("code", "TOSS_ERROR");
			String msg = (String)response.getOrDefault("message", "토스 취소 실패");
			return new CancelResponse(false, 0, null, code, msg);
		} catch (HttpClientErrorException e) {
			log.error("토스 취소 API 호출 실패 - orderId: {}, error: {}", request.orderId(), e.getResponseBodyAsString());
			return new CancelResponse(false, 0, null, "TOSS_CANCEL_ERROR", e.getResponseBodyAsString());
		}
	}
}
