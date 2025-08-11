package wisoft.nextframe.schedulereservationticketing.schedule.domain.performance.exception;

public class AdultOnlyPerformanceException extends RuntimeException {
	public AdultOnlyPerformanceException() {
		super("성인 전용 공연은 성인만 예매할 수 있습니다.");
	}
}
