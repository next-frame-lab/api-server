package wisoft.nextframe.payment.application.payment.exception;

import java.util.UUID;

public class ReservationNotFoundException extends RuntimeException {
	public ReservationNotFoundException(UUID reservationId) {
		super("존재하지 않는 예약입니다. reservationId=" + reservationId);
	}
}
