package wisoft.nextframe.payment.application.payment.outbox.ticketissue;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.payment.application.payment.outbox.AbstractOutboxRetryUseCase;
import wisoft.nextframe.payment.application.payment.outbox.OutboxRepository;
import wisoft.nextframe.payment.application.payment.port.output.TicketingClient;
import wisoft.nextframe.payment.domain.ReservationId;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketIssueRetryUseCase extends AbstractOutboxRetryUseCase<TicketIssueOutboxTarget> {

	private final TicketIssueOutboxRepository outboxRepository;
	private final TicketingClient ticketingClient;

	@Override
	protected OutboxRepository<TicketIssueOutboxTarget> getRepository() {
		return outboxRepository;
	}

	@Override
	protected void executeExternalCall(TicketIssueOutboxTarget target, LocalDateTime now) {
		TicketIssueResult response = ticketingClient.issueTicket(ReservationId.of(target.reservationId()));
		outboxRepository.markSuccess(target.reservationId(), response.ticketId(), now);
		log.info("티켓 발급 재시도 성공 - reservationId={}", target.reservationId());
	}

	@Override
	protected String getLogPrefix() {
		return "ticket_issue_retry";
	}
}
