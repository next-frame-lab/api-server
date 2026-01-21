package wisoft.nextframe.payment.presentation.ticketissue;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.payment.application.ticketissue.TicketIssueRetryUseCase;

@Slf4j
@RestController
@RequiredArgsConstructor
@Profile("dev")
public class TicketIssueRetryAdminController {

	private final TicketIssueRetryUseCase retryUseCase;

	@PostMapping("/admin/ticket-issue-retry/run-once")
	public void runOnce() {
		log.info("manual runOnce called");
		retryUseCase.runOnce();
	}
}