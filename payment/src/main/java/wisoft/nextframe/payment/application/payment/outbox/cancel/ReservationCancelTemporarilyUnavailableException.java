package wisoft.nextframe.payment.application.payment.outbox.cancel;

import wisoft.nextframe.payment.domain.ReservationId;

public class ReservationCancelTemporarilyUnavailableException extends RuntimeException {
	public ReservationCancelTemporarilyUnavailableException(ReservationId reservationId, Throwable cause) {
		super("예약 취소 일시적으로 불가. reservationId=" + reservationId.value(), cause);
	}
}
