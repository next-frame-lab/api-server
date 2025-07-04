package wisoft.nextframe.payment.refund;

import java.time.LocalDateTime;

import wisoft.nextframe.payment.Payment;

//	환불 절차를 통제하는 역할
public class RefundService {

	public Refund refund(Payment payment, LocalDateTime requestAt, LocalDateTime contentStartsAt) {
		Refund refund = payment.refund(requestAt, contentStartsAt);
		// 4. 승인 처리
		refund.approve();

		// 5. 결과 반환
		return refund;
	}

}
