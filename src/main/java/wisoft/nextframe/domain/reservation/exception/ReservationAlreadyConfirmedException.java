package wisoft.nextframe.domain.reservation.exception;

public class ReservationAlreadyConfirmedException extends RuntimeException {
	public ReservationAlreadyConfirmedException() {
		super("이미 확정된 예매입니다.");
	}
}
