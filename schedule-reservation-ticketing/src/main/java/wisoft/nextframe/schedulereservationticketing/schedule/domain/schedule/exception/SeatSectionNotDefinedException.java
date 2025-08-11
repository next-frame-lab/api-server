package wisoft.nextframe.schedulereservationticketing.schedule.domain.schedule.exception;

public class SeatSectionNotDefinedException extends RuntimeException {

	public SeatSectionNotDefinedException() {
		super("공연에 정의되지 않은 좌석 구역입니다.");
	}
}
