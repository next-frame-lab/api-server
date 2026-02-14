package wisoft.nextframe.paymentgateway.provider;

import static org.assertj.core.api.Assertions.*;
import static wisoft.nextframe.paymentgateway.api.PaymentGatewayController.*;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@DisplayName("TossPaymentProvider 단위 테스트")
class TossPaymentProviderTest {

	private MockWebServer mockWebServer;
	private TossPaymentProvider provider;

	@BeforeEach
	void setUp() throws IOException {
		mockWebServer = new MockWebServer();
		mockWebServer.start();

		RestClient restClient = RestClient.builder()
			.baseUrl(mockWebServer.url("/").toString())
			.build();
		provider = new TossPaymentProvider(restClient);
	}

	@AfterEach
	void tearDown() throws IOException {
		mockWebServer.shutdown();
	}

	@Test
	@DisplayName("confirm 성공 시 paymentKey를 저장하고 성공 응답을 반환한다")
	void confirm_success_storesPaymentKey() {
		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setBody("""
				{"status": "DONE", "totalAmount": 10000}
				""")
			.addHeader("Content-Type", "application/json"));

		ConfirmResponse response = provider.confirm(
			new ConfirmRequest("pk_test_123", "order-1", 10000));

		assertThat(response.isSuccess()).isTrue();
		assertThat(response.totalAmount()).isEqualTo(10000);
	}

	@Test
	@DisplayName("confirm 실패 시 에러 정보를 반환한다")
	void confirm_failure_returnsError() {
		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setBody("""
				{"status": "FAILED", "code": "REJECT_CARD_PAYMENT", "message": "카드 거절"}
				""")
			.addHeader("Content-Type", "application/json"));

		ConfirmResponse response = provider.confirm(
			new ConfirmRequest("pk_test_123", "order-1", 10000));

		assertThat(response.isSuccess()).isFalse();
		assertThat(response.errorCode()).isEqualTo("REJECT_CARD_PAYMENT");
	}

	@Test
	@DisplayName("cancel 시 paymentKey가 없으면 PAYMENT_KEY_NOT_FOUND를 반환한다")
	void cancel_noPaymentKey_returnsNotFound() {
		CancelResponse response = provider.cancel(
			new CancelRequest("unknown-order", 10000, "환불"));

		assertThat(response.isSuccess()).isFalse();
		assertThat(response.errorCode()).isEqualTo("PAYMENT_KEY_NOT_FOUND");
	}

	@Test
	@DisplayName("confirm 후 cancel 시 토스 API를 호출하여 성공 응답을 반환한다")
	void cancel_afterConfirm_success() {
		// confirm으로 paymentKey 저장
		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setBody("""
				{"status": "DONE", "totalAmount": 10000}
				""")
			.addHeader("Content-Type", "application/json"));
		provider.confirm(new ConfirmRequest("pk_test_123", "order-1", 10000));

		// cancel 성공
		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setBody("""
				{"status": "CANCELED", "transactionKey": "tx_abc123"}
				""")
			.addHeader("Content-Type", "application/json"));

		CancelResponse response = provider.cancel(
			new CancelRequest("order-1", 10000, "단순 변심"));

		assertThat(response.isSuccess()).isTrue();
		assertThat(response.cancelAmount()).isEqualTo(10000);
		assertThat(response.transactionKey()).isEqualTo("tx_abc123");
	}

	@Test
	@DisplayName("부분 취소(PARTIAL_CANCELED) 상태도 성공으로 처리한다")
	void cancel_partialCanceled_success() {
		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setBody("""
				{"status": "DONE", "totalAmount": 10000}
				""")
			.addHeader("Content-Type", "application/json"));
		provider.confirm(new ConfirmRequest("pk_test_123", "order-1", 10000));

		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setBody("""
				{"status": "PARTIAL_CANCELED", "transactionKey": "tx_partial"}
				""")
			.addHeader("Content-Type", "application/json"));

		CancelResponse response = provider.cancel(
			new CancelRequest("order-1", 6000, "부분 환불"));

		assertThat(response.isSuccess()).isTrue();
		assertThat(response.cancelAmount()).isEqualTo(6000);
	}

	@Test
	@DisplayName("cancel 시 토스 API가 실패 상태를 반환하면 실패 응답을 반환한다")
	void cancel_tossReturnsFailure() {
		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setBody("""
				{"status": "DONE", "totalAmount": 10000}
				""")
			.addHeader("Content-Type", "application/json"));
		provider.confirm(new ConfirmRequest("pk_test_123", "order-1", 10000));

		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setBody("""
				{"status": "FAILED", "code": "ALREADY_CANCELED_PAYMENT", "message": "이미 취소된 결제"}
				""")
			.addHeader("Content-Type", "application/json"));

		CancelResponse response = provider.cancel(
			new CancelRequest("order-1", 10000, "환불"));

		assertThat(response.isSuccess()).isFalse();
		assertThat(response.errorCode()).isEqualTo("ALREADY_CANCELED_PAYMENT");
	}

	@Test
	@DisplayName("cancel 시 토스 API가 HTTP 에러를 반환하면 TOSS_CANCEL_ERROR를 반환한다")
	void cancel_httpError_returnsError() {
		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setBody("""
				{"status": "DONE", "totalAmount": 10000}
				""")
			.addHeader("Content-Type", "application/json"));
		provider.confirm(new ConfirmRequest("pk_test_123", "order-1", 10000));

		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(400)
			.setBody("""
				{"code": "INVALID_REQUEST", "message": "잘못된 요청"}
				""")
			.addHeader("Content-Type", "application/json"));

		CancelResponse response = provider.cancel(
			new CancelRequest("order-1", 10000, "환불"));

		assertThat(response.isSuccess()).isFalse();
		assertThat(response.errorCode()).isEqualTo("TOSS_CANCEL_ERROR");
	}
}
