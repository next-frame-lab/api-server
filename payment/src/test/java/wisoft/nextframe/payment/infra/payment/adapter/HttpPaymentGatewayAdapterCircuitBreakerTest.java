package wisoft.nextframe.payment.infra.payment.adapter;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import wisoft.nextframe.payment.application.payment.exception.PaymentGatewayExternalCallFailedException;
import wisoft.nextframe.payment.application.payment.exception.PaymentGatewayTemporarilyUnavailableException;
import wisoft.nextframe.payment.application.payment.port.output.PaymentGateway;

@SpringBootTest(classes = HttpPaymentGatewayAdapterCircuitBreakerTest.TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HttpPaymentGatewayAdapterCircuitBreakerTest {

	static final MockWebServer mockWebServer;

	static {
		mockWebServer = new MockWebServer();
		try {
			mockWebServer.start();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Configuration
	@Import({AopAutoConfiguration.class, CircuitBreakerAutoConfiguration.class})
	static class TestConfig {

		@Bean
		PaymentGateway paymentGateway() {
			String baseUrl = mockWebServer.url("/").toString();
			return new HttpPaymentGatewayAdapter(RestClient.builder(), baseUrl);
		}
	}

	@Autowired
	private PaymentGateway paymentGateway;

	@Autowired
	private CircuitBreakerRegistry circuitBreakerRegistry;

	@DynamicPropertySource
	static void overrideProperties(DynamicPropertyRegistry registry) {
		registry.add("resilience4j.circuitbreaker.instances.paymentGateway.slidingWindowType", () -> "COUNT_BASED");
		registry.add("resilience4j.circuitbreaker.instances.paymentGateway.slidingWindowSize", () -> "5");
		registry.add("resilience4j.circuitbreaker.instances.paymentGateway.minimumNumberOfCalls", () -> "3");
		registry.add("resilience4j.circuitbreaker.instances.paymentGateway.failureRateThreshold", () -> "50");
		registry.add("resilience4j.circuitbreaker.instances.paymentGateway.waitDurationInOpenState", () -> "60s");
		registry.add("resilience4j.circuitbreaker.instances.paymentGateway.permittedNumberOfCallsInHalfOpenState", () -> "1");
		registry.add("resilience4j.circuitbreaker.instances.paymentGateway.recordExceptions[0]",
			() -> "org.springframework.web.client.ResourceAccessException");
	}

	@AfterAll
	static void stopServer() throws IOException {
		mockWebServer.shutdown();
	}

	@BeforeEach
	void resetCircuitBreaker() {
		CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("paymentGateway");
		cb.reset();
	}

	@Test
	@DisplayName("서버 오류 시 CB fallback으로 ExternalCallFailedException이 발생한다")
	void serverError_triggersFallback_externalCallFailed() {
		mockWebServer.enqueue(new MockResponse().setResponseCode(500));

		assertThatThrownBy(() -> paymentGateway.confirmPayment("key-1", "order-1", 10000))
			.isInstanceOf(PaymentGatewayExternalCallFailedException.class);
	}

	@Test
	@DisplayName("cancelPayment 서버 오류 시에도 ExternalCallFailedException이 발생한다")
	void cancelServerError_triggersFallback() {
		mockWebServer.enqueue(new MockResponse().setResponseCode(500));

		assertThatThrownBy(() -> paymentGateway.cancelPayment("pk_test_key", "order-1", 5000, "테스트"))
			.isInstanceOf(PaymentGatewayExternalCallFailedException.class);
	}

	@Test
	@DisplayName("CB가 OPEN 상태이면 confirmPayment에서 TemporarilyUnavailableException이 발생한다")
	void circuitBreakerOpen_confirm_throwsTemporarilyUnavailable() {
		CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("paymentGateway");
		cb.transitionToOpenState();

		assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);

		assertThatThrownBy(() -> paymentGateway.confirmPayment("key-1", "order-1", 10000))
			.isInstanceOf(PaymentGatewayTemporarilyUnavailableException.class);
	}

	@Test
	@DisplayName("CB가 OPEN 상태이면 cancelPayment에서 TemporarilyUnavailableException이 발생한다")
	void circuitBreakerOpen_cancel_throwsTemporarilyUnavailable() {
		CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("paymentGateway");
		cb.transitionToOpenState();

		assertThatThrownBy(() -> paymentGateway.cancelPayment("pk_test_key", "order-1", 5000, "테스트"))
			.isInstanceOf(PaymentGatewayTemporarilyUnavailableException.class);
	}

	@Test
	@DisplayName("CB가 CLOSED 상태이면 정상 응답을 반환한다")
	void circuitBreakerClosed_returnsNormalResponse() {
		mockWebServer.enqueue(new MockResponse()
			.setBody("""
				{"isSuccess": true, "totalAmount": 10000, "errorCode": null, "errorMessage": null}
				""")
			.setHeader("Content-Type", "application/json"));

		var result = paymentGateway.confirmPayment("key-1", "order-1", 10000);

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.totalAmount()).isEqualTo(10000);
	}
}
