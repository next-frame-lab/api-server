package wisoft.nextframe.performance.exception;

public class InvalidReservationTimeException extends RuntimeException {
	public InvalidReservationTimeException() {
		super("예매 가능한 시간이 아닙니다.");
	}
}
