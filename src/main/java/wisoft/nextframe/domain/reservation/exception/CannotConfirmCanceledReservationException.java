package wisoft.nextframe.domain.reservation.exception;

public class CannotConfirmCanceledReservationException extends RuntimeException {
	public CannotConfirmCanceledReservationException() {
		super("취소된 예매는 확정할 수 없습니다.");
	}
}
