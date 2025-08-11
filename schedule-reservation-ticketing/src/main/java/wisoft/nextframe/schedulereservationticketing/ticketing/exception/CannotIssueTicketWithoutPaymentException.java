package wisoft.nextframe.schedulereservationticketing.ticketing.exception;

public class CannotIssueTicketWithoutPaymentException extends RuntimeException {
	public CannotIssueTicketWithoutPaymentException() {
		super("결제 완료된 상태가 아닐 경우, 티켓을 발급할 수 없습니다.");
	}
}