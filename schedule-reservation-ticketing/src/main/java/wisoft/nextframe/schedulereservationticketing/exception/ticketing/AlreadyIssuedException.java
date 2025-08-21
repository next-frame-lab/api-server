package wisoft.nextframe.schedulereservationticketing.exception.ticketing;

import wisoft.nextframe.schedulereservationticketing.entity.ticketing.ReservationId;

public class AlreadyIssuedException extends RuntimeException {
	public AlreadyIssuedException(ReservationId reservationId) {
		super("이미 발급된 티켓입니다. 예약 ID: " + reservationId.getValue());
	}
}
