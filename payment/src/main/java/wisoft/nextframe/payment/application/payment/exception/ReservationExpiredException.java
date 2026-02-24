package wisoft.nextframe.payment.application.payment.exception;

import java.util.UUID;

public class ReservationExpiredException extends RuntimeException {
	public ReservationExpiredException(UUID reservationId) {
		super("만료되었거나 이미 처리된 예약입니다. reservationId=" + reservationId);
	}
}
