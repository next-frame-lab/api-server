package wisoft.nextframe.schedulereservationticketing.exception.reservation;

public class SeatAlreadyLockedException extends RuntimeException {
	public SeatAlreadyLockedException(String message) {
		super(message);
	}
}
