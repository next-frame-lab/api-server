package wisoft.nextframe.payment.application.ticketissue.exception;

import wisoft.nextframe.payment.domain.ReservationId;

public class TicketIssueTemporarilyUnavailableException extends RuntimeException {
    public TicketIssueTemporarilyUnavailableException(ReservationId reservationId, Throwable cause) {
        super("티켓 발급이 일시적으로 불가능합니다. reservationId=" + reservationId.value(), cause);
    }
}
