package wisoft.nextframe.refund;

import java.time.LocalDateTime;

import wisoft.nextframe.payment.Payment;

//	환불 절차를 통제하는 역할
public class RefundService {

	public Refund refund(Payment payment, LocalDateTime requestAt, LocalDateTime contentStartsAt) {
		return Refund.refund(payment, requestAt, contentStartsAt);
	}

}
