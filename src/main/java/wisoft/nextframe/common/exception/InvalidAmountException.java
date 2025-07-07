package wisoft.nextframe.common.exception;

public class InvalidAmountException extends RuntimeException {
	public InvalidAmountException() {
		super("결제 금액은 0보다 커야 합니다.");
	}
}