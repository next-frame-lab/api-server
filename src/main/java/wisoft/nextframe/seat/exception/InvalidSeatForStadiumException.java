package wisoft.nextframe.seat.exception;

public class InvalidSeatForStadiumException extends RuntimeException {
	public InvalidSeatForStadiumException() {
		super("해당 공연장의 좌석이 아닙니다.");
	}
}
