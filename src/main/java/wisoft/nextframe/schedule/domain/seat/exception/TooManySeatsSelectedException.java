package wisoft.nextframe.schedule.domain.seat.exception;

public class TooManySeatsSelectedException extends RuntimeException {
	public TooManySeatsSelectedException() {
		super("좌석은 최대 4개까지만 선택할 수 있습니다.");
	}
}
