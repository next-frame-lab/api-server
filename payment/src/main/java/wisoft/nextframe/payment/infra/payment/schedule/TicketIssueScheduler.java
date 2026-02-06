package wisoft.nextframe.payment.infra.payment.schedule;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.payment.application.payment.outbox.ticketissue.TicketIssueRetryUseCase;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class TicketIssueScheduler {

	private final TicketIssueRetryUseCase retryUseCase;

	@Scheduled(fixedDelayString = "${ticket.issue.retry.delay-ms}")
	public void retryTicketIssue() {
		if (!retryUseCase.hasPending()) {
			log.debug("ticket issue retry skipped. no pending");
			return;
		}

		log.debug("ticket issue retry start");
		retryUseCase.runOnce();
	}
}
