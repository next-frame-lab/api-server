package wisoft.nextframe.seat.exception;

public class NoSeatSelectedException extends RuntimeException {
	public NoSeatSelectedException() {
		super("좌석은 최소 1개 이상 선택해야 합니다.");
	}
}
