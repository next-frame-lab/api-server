package wisoft.nextframe.seat.exception;

public class SeatAlreadyLockedException extends RuntimeException {
	public SeatAlreadyLockedException() {
		super("이미 예약된 좌석은 선택할 수 없습니다.");
	}
}
