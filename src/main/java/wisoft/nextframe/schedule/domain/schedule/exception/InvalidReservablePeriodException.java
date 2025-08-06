package wisoft.nextframe.schedule.domain.schedule.exception;

public class InvalidReservablePeriodException extends RuntimeException {
	public InvalidReservablePeriodException() {
		super("예매 가능한 시간이 아닙니다.");
	}
}
