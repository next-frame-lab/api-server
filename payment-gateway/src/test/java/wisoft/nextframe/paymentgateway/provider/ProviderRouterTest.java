package wisoft.nextframe.paymentgateway.provider;

import static org.assertj.core.api.Assertions.*;
import static wisoft.nextframe.paymentgateway.api.PaymentGatewayController.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ProviderRouter 단위 테스트")
class ProviderRouterTest {

	private final StubPaymentProvider stubProvider = new StubPaymentProvider();
	private final ProviderRouter router = new ProviderRouter(List.of(stubProvider));

	@Test
	@DisplayName("지원하는 provider로 confirm 요청 시 정상 라우팅된다")
	void confirm_routesToCorrectProvider() {
		ConfirmRequest request = new ConfirmRequest("key", "order-1", 10000);

		ConfirmResponse response = router.confirm("toss", request);

		assertThat(response.isSuccess()).isTrue();
		assertThat(response.totalAmount()).isEqualTo(10000);
	}

	@Test
	@DisplayName("지원하는 provider로 cancel 요청 시 정상 라우팅된다")
	void cancel_routesToCorrectProvider() {
		CancelRequest request = new CancelRequest("order-1", 10000, "환불 사유");

		CancelResponse response = router.cancel("toss", request);

		assertThat(response.isSuccess()).isTrue();
		assertThat(response.cancelAmount()).isEqualTo(10000);
	}

	@Test
	@DisplayName("지원하지 않는 provider로 confirm 요청 시 예외가 발생한다")
	void confirm_unsupportedProvider_throwsException() {
		ConfirmRequest request = new ConfirmRequest("key", "order-1", 10000);

		assertThatThrownBy(() -> router.confirm("unknown", request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("지원하지 않는 결제수단입니다.");
	}

	@Test
	@DisplayName("지원하지 않는 provider로 cancel 요청 시 예외가 발생한다")
	void cancel_unsupportedProvider_throwsException() {
		CancelRequest request = new CancelRequest("order-1", 10000, "환불 사유");

		assertThatThrownBy(() -> router.cancel("unknown", request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("지원하지 않는 결제수단입니다.");
	}
}