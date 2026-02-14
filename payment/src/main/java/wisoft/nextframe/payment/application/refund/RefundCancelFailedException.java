package wisoft.nextframe.payment.application.refund;

import lombok.Getter;

@Getter
public class RefundCancelFailedException extends RuntimeException {

	private final String errorCode;

	public RefundCancelFailedException(String errorCode) {
		super("PG 환불 요청이 실패했습니다. errorCode=" + errorCode);
		this.errorCode = errorCode;
	}
}
