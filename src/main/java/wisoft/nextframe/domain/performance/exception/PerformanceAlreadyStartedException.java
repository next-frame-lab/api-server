package wisoft.nextframe.domain.performance.exception;

public class PerformanceAlreadyStartedException extends RuntimeException {
	public PerformanceAlreadyStartedException() {
		super("이미 시작된 공연은 예매할 수 없습니다.");
	}
}
