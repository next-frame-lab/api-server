package wisoft.nextframe.schedulereservationticketing.ticketing.service;

import wisoft.nextframe.schedulereservationticketing.reservation.ReservationId;

public class AlreadyIssuedException extends RuntimeException {
	public AlreadyIssuedException(ReservationId reservationId) {
		super("이미 발급된 티켓입니다. 예약 ID: " + reservationId.getValue());
	}
}
