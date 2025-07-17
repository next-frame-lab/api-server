package wisoft.nextframe.reservation.exception;

public class ReservationAlreadyCanceledException extends RuntimeException {
	public ReservationAlreadyCanceledException() {
		super("이미 취소된 예매입니다.");
	}
}
