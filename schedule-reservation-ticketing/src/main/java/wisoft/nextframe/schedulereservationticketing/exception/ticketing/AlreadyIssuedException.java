package wisoft.nextframe.schedulereservationticketing.exception.ticketing;

import java.util.UUID;

public class AlreadyIssuedException extends RuntimeException {
	public AlreadyIssuedException(UUID reservationId) {
		super("이미 발급된 티켓입니다. 예약 ID: " + reservationId);
	}
}
