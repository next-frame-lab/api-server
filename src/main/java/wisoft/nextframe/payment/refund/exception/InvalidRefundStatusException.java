package wisoft.nextframe.payment.refund.exception;

public class InvalidRefundStatusException extends RefundException {
	public InvalidRefundStatusException(String action) {
		super(action + "은 REQUESTED 상태에서만 가능합니다.");
	}
}
