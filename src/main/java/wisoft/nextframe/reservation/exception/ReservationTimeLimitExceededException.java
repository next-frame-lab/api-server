package wisoft.nextframe.reservation.exception;

public class ReservationTimeLimitExceededException extends RuntimeException {
	public ReservationTimeLimitExceededException() {
		super("예매 시작 후 10분이 지나면 예매할 수 없습니다.");
	}
}
