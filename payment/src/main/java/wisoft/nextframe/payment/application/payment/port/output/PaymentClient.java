
package wisoft.nextframe.payment.application.payment.port.output;

import java.util.Map;

public interface PaymentClient {

	/**
	 * 외부 결제 시스템에 결제를 요청한다.
	 *
	 * @param orderId    주문 ID
	 * @param amount     결제 금액
	 * @param paymentKey 결제 키
	 * @return 결제 확인 결과를 포함하는 Map
	 */
	Map<String, Object> confirmPayment(String paymentKey, String orderId, int amount);
}