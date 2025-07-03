package wisoft.nextframe.payment.domain.payment.exception;

import lombok.Getter;

@Getter
public class PaymentConfirmedException extends PaymentException {

	private final String errorCode; // Toss(또는 외부결제사)에서 받은 에러코드

	public PaymentConfirmedException(String errorCode) {
		super(errorCode);
		this.errorCode = errorCode;
	}
}
