package wisoft.nextframe.payment.application.ticketissue.exception;

import wisoft.nextframe.payment.domain.ReservationId;

public class TicketIssueInvalidResponseException extends RuntimeException {
	public TicketIssueInvalidResponseException(ReservationId reservationId) {
		super("티켓 발급 응답이 유효하지 않습니다. reservationId=" + reservationId.value());
	}
}