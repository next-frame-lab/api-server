package wisoft.nextframe.paymentgateway.api;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static wisoft.nextframe.paymentgateway.api.PaymentGatewayController.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import wisoft.nextframe.paymentgateway.provider.ProviderRouter;

@WebMvcTest(PaymentGatewayController.class)
@DisplayName("PaymentGatewayController 테스트")
class PaymentGatewayControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ProviderRouter router;

	@Test
	@DisplayName("POST /payments/confirm 성공 시 200 OK를 반환한다")
	void confirm_success() throws Exception {
		given(router.confirm(eq("toss"), any(ConfirmRequest.class)))
			.willReturn(new ConfirmResponse(true, 10000, null, null));

		mockMvc.perform(post("/api/v1/gateway/payments/confirm")
				.param("provider", "toss")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"paymentKey": "pk_123", "orderId": "order-1", "amount": 10000}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.totalAmount").value(10000));
	}

	@Test
	@DisplayName("POST /payments/cancel 성공 시 200 OK를 반환한다")
	void cancel_success() throws Exception {
		given(router.cancel(eq("toss"), any(CancelRequest.class)))
			.willReturn(new CancelResponse(true, 10000, "tx_abc", null, null));

		mockMvc.perform(post("/api/v1/gateway/payments/cancel")
				.param("provider", "toss")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"orderId": "order-1", "cancelAmount": 10000, "cancelReason": "단순 변심"}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.cancelAmount").value(10000))
			.andExpect(jsonPath("$.transactionKey").value("tx_abc"));
	}

	@Test
	@DisplayName("POST /payments/cancel 실패 시에도 200 OK에 에러 정보를 반환한다")
	void cancel_failure() throws Exception {
		given(router.cancel(eq("toss"), any(CancelRequest.class)))
			.willReturn(new CancelResponse(false, 0, null, "ALREADY_CANCELED", "이미 취소됨"));

		mockMvc.perform(post("/api/v1/gateway/payments/cancel")
				.param("provider", "toss")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"orderId": "order-1", "cancelAmount": 10000, "cancelReason": "환불"}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(false))
			.andExpect(jsonPath("$.errorCode").value("ALREADY_CANCELED"));
	}

	@Test
	@DisplayName("provider 파라미터 생략 시 기본값 toss로 동작한다")
	void cancel_defaultProvider() throws Exception {
		given(router.cancel(eq("toss"), any(CancelRequest.class)))
			.willReturn(new CancelResponse(true, 5000, "tx_def", null, null));

		mockMvc.perform(post("/api/v1/gateway/payments/cancel")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"orderId": "order-2", "cancelAmount": 5000, "cancelReason": "테스트"}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true));
	}
}