package wisoft.nextframe.payment.infra.payment.adapter;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import wisoft.nextframe.payment.application.payment.port.output.PaymentGateway.PaymentCancelResult;
import wisoft.nextframe.payment.application.payment.port.output.PaymentGateway.PaymentConfirmResult;

class HttpPaymentGatewayAdapterTest {

	private MockWebServer mockWebServer;
	private HttpPaymentGatewayAdapter adapter;

	@BeforeEach
	void setUp() throws IOException {
		mockWebServer = new MockWebServer();
		mockWebServer.start();

		String baseUrl = mockWebServer.url("/").toString();
		adapter = new HttpPaymentGatewayAdapter(RestClient.builder(), baseUrl);
	}

	@AfterEach
	void tearDown() throws IOException {
		mockWebServer.shutdown();
	}

	@Nested
	@DisplayName("결제 승인 (confirmPayment)")
	class ConfirmPayment {

		@Test
		@DisplayName("성공 응답을 정상적으로 파싱한다")
		void successResponse() {
			mockWebServer.enqueue(new MockResponse()
				.setBody("""
					{"isSuccess": true, "totalAmount": 10000, "errorCode": null, "errorMessage": null}
					""")
				.setHeader("Content-Type", "application/json"));

			PaymentConfirmResult result = adapter.confirmPayment("key-1", "order-1", 10000);

			assertThat(result.isSuccess()).isTrue();
			assertThat(result.totalAmount()).isEqualTo(10000);
			assertThat(result.errorCode()).isNull();
			assertThat(result.errorMessage()).isNull();
		}

		@Test
		@DisplayName("실패 응답을 정상적으로 파싱한다")
		void failureResponse() {
			mockWebServer.enqueue(new MockResponse()
				.setBody("""
					{"isSuccess": false, "totalAmount": 0, "errorCode": "INVALID_AMOUNT", "errorMessage": "금액 불일치"}
					""")
				.setHeader("Content-Type", "application/json"));

			PaymentConfirmResult result = adapter.confirmPayment("key-1", "order-1", 10000);

			assertThat(result.isSuccess()).isFalse();
			assertThat(result.errorCode()).isEqualTo("INVALID_AMOUNT");
			assertThat(result.errorMessage()).isEqualTo("금액 불일치");
		}

		@Test
		@DisplayName("잘못된 JSON 응답이면 PARSE_ERROR를 반환한다")
		void invalidJsonResponse() {
			mockWebServer.enqueue(new MockResponse()
				.setBody("not a json")
				.setHeader("Content-Type", "application/json"));

			PaymentConfirmResult result = adapter.confirmPayment("key-1", "order-1", 10000);

			assertThat(result.isSuccess()).isFalse();
			assertThat(result.errorCode()).isEqualTo("PARSE_ERROR");
		}

		@Test
		@DisplayName("읽기 타임아웃이 발생하면 예외가 발생한다")
		void readTimeout() {
			mockWebServer.enqueue(new MockResponse()
				.setBody("{}")
				.setHeadersDelay(6, TimeUnit.SECONDS));

			assertThatThrownBy(() -> adapter.confirmPayment("key-1", "order-1", 10000))
				.isInstanceOf(Exception.class);
		}
	}

	@Nested
	@DisplayName("결제 취소 (cancelPayment)")
	class CancelPayment {

		@Test
		@DisplayName("성공 응답을 정상적으로 파싱한다")
		void successResponse() {
			mockWebServer.enqueue(new MockResponse()
				.setBody("""
					{"isSuccess": true, "cancelAmount": 5000, "transactionKey": "txn-1", "errorCode": null, "errorMessage": null}
					""")
				.setHeader("Content-Type", "application/json"));

			PaymentCancelResult result = adapter.cancelPayment("order-1", 5000, "고객 요청");

			assertThat(result.isSuccess()).isTrue();
			assertThat(result.cancelAmount()).isEqualTo(5000);
			assertThat(result.transactionKey()).isEqualTo("txn-1");
			assertThat(result.errorCode()).isNull();
		}

		@Test
		@DisplayName("실패 응답을 정상적으로 파싱한다")
		void failureResponse() {
			mockWebServer.enqueue(new MockResponse()
				.setBody("""
					{"isSuccess": false, "cancelAmount": 0, "transactionKey": null, "errorCode": "ALREADY_CANCELED", "errorMessage": "이미 취소됨"}
					""")
				.setHeader("Content-Type", "application/json"));

			PaymentCancelResult result = adapter.cancelPayment("order-1", 5000, "고객 요청");

			assertThat(result.isSuccess()).isFalse();
			assertThat(result.errorCode()).isEqualTo("ALREADY_CANCELED");
		}

		@Test
		@DisplayName("잘못된 JSON 응답이면 PARSE_ERROR를 반환한다")
		void invalidJsonResponse() {
			mockWebServer.enqueue(new MockResponse()
				.setBody("broken")
				.setHeader("Content-Type", "application/json"));

			PaymentCancelResult result = adapter.cancelPayment("order-1", 5000, "고객 요청");

			assertThat(result.isSuccess()).isFalse();
			assertThat(result.errorCode()).isEqualTo("PARSE_ERROR");
		}

		@Test
		@DisplayName("읽기 타임아웃이 발생하면 예외가 발생한다")
		void readTimeout() {
			mockWebServer.enqueue(new MockResponse()
				.setBody("{}")
				.setHeadersDelay(6, TimeUnit.SECONDS));

			assertThatThrownBy(() -> adapter.cancelPayment("order-1", 5000, "고객 요청"))
				.isInstanceOf(Exception.class);
		}
	}

	@Nested
	@DisplayName("서버 오류 응답")
	class ServerError {

		@Test
		@DisplayName("500 응답이면 예외가 발생한다")
		void confirm_serverError() {
			mockWebServer.enqueue(new MockResponse().setResponseCode(500));

			assertThatThrownBy(() -> adapter.confirmPayment("key-1", "order-1", 10000))
				.isInstanceOf(Exception.class);
		}

		@Test
		@DisplayName("500 응답이면 취소도 예외가 발생한다")
		void cancel_serverError() {
			mockWebServer.enqueue(new MockResponse().setResponseCode(500));

			assertThatThrownBy(() -> adapter.cancelPayment("order-1", 5000, "고객 요청"))
				.isInstanceOf(Exception.class);
		}
	}
}
