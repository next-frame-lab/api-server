
package wisoft.nextframe.payment.application.port.output;

import wisoft.nextframe.payment.domain.Payment;

public interface ExternalPaymentClient {

	/**
	 * 외부 결제 시스템에 결제를 요청한다.
	 *
	 * @param payment 결제 정보
	 * @return 결제 성공 여부
	 */
	boolean requestPayment(Payment payment);
}