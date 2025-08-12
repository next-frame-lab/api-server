package wisoft.nextframe.payment.application.refund;

import java.time.LocalDateTime;

import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.payment.domain.payment.PaymentIssuer;
import wisoft.nextframe.payment.domain.refund.Refund;

//	환불 절차를 통제하는 역할
public class RefundService {

	private final PaymentIssuer paymentIssuer;

	public RefundService(PaymentIssuer paymentIssuer) {
		this.paymentIssuer = paymentIssuer;
	}

	public Refund refund(Payment payment, LocalDateTime requestAt, LocalDateTime contentStartsAt) {
		// 1. 정책에 따라 환불 가능 여부 판단 및 Refund 생성
		if (payment == null) {
			throw new IllegalArgumentException("Payment cannot be null");
		}
		Refund refund = paymentIssuer.issueRefund(payment, requestAt, contentStartsAt);

		// 2. 승인 처리
		refund.approve();

		// 3. 결과 반환
		return refund;
	}

}