package wisoft.nextframe.paymentgateway.provider;

import static org.assertj.core.api.Assertions.*;
import static wisoft.nextframe.paymentgateway.api.PaymentGatewayController.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("StubPaymentProvider 단위 테스트")
class StubPaymentProviderTest {

	private final StubPaymentProvider provider = new StubPaymentProvider();

	@Test
	@DisplayName("toss provider를 지원한다")
	void supports_toss() {
		assertThat(provider.supports("toss")).isTrue();
		assertThat(provider.supports("TOSS")).isTrue();
	}

	@Test
	@DisplayName("toss 외 provider는 지원하지 않는다")
	void doesNotSupport_other() {
		assertThat(provider.supports("kakaopay")).isFalse();
	}

	@Test
	@DisplayName("confirm은 항상 성공을 반환한다")
	void confirm_alwaysSuccess() {
		ConfirmRequest request = new ConfirmRequest("key", "order-1", 10000);

		ConfirmResponse response = provider.confirm(request);

		assertThat(response.isSuccess()).isTrue();
		assertThat(response.totalAmount()).isEqualTo(10000);
		assertThat(response.errorCode()).isNull();
	}

	@Test
	@DisplayName("cancel은 항상 성공을 반환한다")
	void cancel_alwaysSuccess() {
		CancelRequest request = new CancelRequest("pk_stub_key", "order-1", 10000, "테스트 환불");

		CancelResponse response = provider.cancel(request);

		assertThat(response.isSuccess()).isTrue();
		assertThat(response.cancelAmount()).isEqualTo(10000);
		assertThat(response.transactionKey()).isEqualTo("stub-tx-key");
		assertThat(response.errorCode()).isNull();
	}
}