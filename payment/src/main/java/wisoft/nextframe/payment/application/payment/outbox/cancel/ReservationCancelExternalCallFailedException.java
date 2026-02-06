package wisoft.nextframe.payment.application.payment.outbox.cancel;

import wisoft.nextframe.payment.domain.ReservationId;

public class ReservationCancelExternalCallFailedException extends RuntimeException {
	public ReservationCancelExternalCallFailedException(ReservationId reservationId, Throwable cause) {
		super("예약 취소 외부 호출 실패. reservationId=" + reservationId.value(), cause);
	}
}
