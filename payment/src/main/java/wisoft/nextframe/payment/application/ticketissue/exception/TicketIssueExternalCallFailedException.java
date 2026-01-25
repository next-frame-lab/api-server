package wisoft.nextframe.payment.application.ticketissue.exception;

import wisoft.nextframe.payment.domain.ReservationId;

public class TicketIssueExternalCallFailedException extends RuntimeException {
	public TicketIssueExternalCallFailedException(ReservationId reservationId, Throwable cause) {
		super("티켓 발급 외부 호출 실패. reservationId=" + reservationId.value(), cause);
	}
}
