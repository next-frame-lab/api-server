package wisoft.nextframe.payment.domain.refund.exception;

public class NotRefundableException extends RefundException {
	public NotRefundableException() {
		super("공연 시작 1시간 전에는 환불할 수 없습니다.");
	}
}
