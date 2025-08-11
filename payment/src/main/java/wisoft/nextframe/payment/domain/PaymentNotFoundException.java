package wisoft.nextframe.payment.domain;

import wisoft.nextframe.payment.domain.exception.PaymentException;

public class PaymentNotFoundException extends PaymentException {
	public PaymentNotFoundException() {
		super("해당 결제 정보를 찾을 수 없습니다.");
	}
}
