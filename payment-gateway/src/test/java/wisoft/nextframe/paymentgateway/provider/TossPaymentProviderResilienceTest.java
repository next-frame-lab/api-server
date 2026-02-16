package wisoft.nextframe.paymentgateway.provider;

import static org.assertj.core.api.Assertions.*;
import static wisoft.nextframe.paymentgateway.api.PaymentGatewayController.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;

@DisplayName("TossPaymentProvider 복원력 패턴 통합 테스트")
@SpringBootTest
@ActiveProfiles("prod")
class TossPaymentProviderResilienceTest {

	static MockWebServer mockWebServer;

	@Autowired
	PaymentProvider provider;

	@Autowired
	CircuitBreakerRegistry circuitBreakerRegistry;

	@DynamicPropertySource
	static void properties(DynamicPropertyRegistry registry) {
		mockWebServer = new MockWebServer();
		try {
			mockWebServer.start();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		registry.add("toss.base-url", () -> mockWebServer.url("/").toString());
		registry.add("toss.secret-key", () -> "test_secret_key");
	}

	@BeforeEach
	void resetCircuitBreaker() {
		circuitBreakerRegistry.circuitBreaker("tossConfirm").reset();
	}

	@AfterAll
	static void tearDown() throws IOException {
		mockWebServer.shutdown();
	}

	@Test
	@DisplayName("readTimeout 초과 시 PG_NETWORK_ERROR fallback을 반환한다")
	void confirm_timeout_returnsFallback() {
		mockWebServer.enqueue(new MockResponse()
			.setBodyDelay(5, TimeUnit.SECONDS)
			.setResponseCode(200)
			.setBody("""
				{"status": "DONE", "totalAmount": 1000}
				""")
			.addHeader("Content-Type", "application/json"));

		ConfirmResponse response = provider.confirm(
			new ConfirmRequest("pk_timeout", "order-timeout", 1000));

		assertThat(response.isSuccess()).isFalse();
		assertThat(response.errorCode()).isEqualTo("PG_NETWORK_ERROR");
		assertThat(response.errorMessage()).contains("PG 통신 중 오류");
	}

	@Test
	@DisplayName("연속 네트워크 실패 후 서킷 브레이커가 OPEN 상태가 되면 PG_TEMPORARY_UNAVAILABLE을 반환한다")
	void confirm_circuitBreakerOpens_afterNetworkFailures() {
		// minimumNumberOfCalls=2, failureRateThreshold=50
		// 연결 끊김 2회 → ResourceAccessException → 실패율 100% → CB OPEN
		for (int i = 0; i < 2; i++) {
			mockWebServer.enqueue(new MockResponse()
				.setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST));

			provider.confirm(new ConfirmRequest("pk_" + i, "order-cb-" + i, 1000));
		}

		CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("tossConfirm");
		assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);

		// OPEN 상태에서는 PG 호출 없이 즉시 fallback 반환
		ConfirmResponse response = provider.confirm(
			new ConfirmRequest("pk_blocked", "order-blocked", 1000));

		assertThat(response.isSuccess()).isFalse();
		assertThat(response.errorCode()).isEqualTo("PG_TEMPORARY_UNAVAILABLE");
		assertThat(response.errorMessage()).contains("일시적으로 불안정");
	}

	@Test
	@DisplayName("성공 호출은 서킷 브레이커를 OPEN 상태로 전이시키지 않는다")
	void confirm_successDoesNotOpenCircuitBreaker() {
		for (int i = 0; i < 4; i++) {
			mockWebServer.enqueue(new MockResponse()
				.setResponseCode(200)
				.setBody("""
					{"status": "DONE", "totalAmount": 1000}
					""")
				.addHeader("Content-Type", "application/json"));

			ConfirmResponse response = provider.confirm(
				new ConfirmRequest("pk_" + i, "order-ok-" + i, 1000));

			assertThat(response.isSuccess()).isTrue();
		}

		CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("tossConfirm");
		assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
	}

	@Test
	@DisplayName("네트워크 실패와 성공이 혼합될 때 실패율이 임계치 미만이면 서킷 브레이커는 CLOSED 상태를 유지한다")
	void confirm_mixedResults_circuitBreakerRemainsClosed() {
		// slidingWindowSize=4, minimumNumberOfCalls=2, failureRateThreshold=50
		// 성공 → 성공 → 실패 → 성공 순으로 호출하면
		// 최종 실패율 1/4 = 25% < 50% → CLOSED 유지
		// (실패를 먼저 넣으면 2회차(1/2=50%) 시점에 CB가 OPEN되므로 성공을 먼저 배치)

		// 2회 성공
		for (int i = 0; i < 2; i++) {
			mockWebServer.enqueue(new MockResponse()
				.setResponseCode(200)
				.setBody("""
					{"status": "DONE", "totalAmount": 1000}
					""")
				.addHeader("Content-Type", "application/json"));
			provider.confirm(new ConfirmRequest("pk_" + i, "order-mix-" + i, 1000));
		}

		// 1회 네트워크 실패
		mockWebServer.enqueue(new MockResponse()
			.setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST));
		provider.confirm(new ConfirmRequest("pk_fail", "order-mix-2", 1000));

		// 1회 성공
		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setBody("""
				{"status": "DONE", "totalAmount": 1000}
				""")
			.addHeader("Content-Type", "application/json"));
		provider.confirm(new ConfirmRequest("pk_3", "order-mix-3", 1000));

		CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("tossConfirm");
		assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
	}

	@Test
	@DisplayName("PG 서버가 응답하지 않으면 PG_NETWORK_ERROR를 반환한다")
	void confirm_noResponse_returnsFallback() {
		mockWebServer.enqueue(new MockResponse()
			.setSocketPolicy(SocketPolicy.NO_RESPONSE));

		ConfirmResponse response = provider.confirm(
			new ConfirmRequest("pk_dead", "order-dead", 1000));

		assertThat(response.isSuccess()).isFalse();
		assertThat(response.errorCode()).isEqualTo("PG_NETWORK_ERROR");
	}
}
