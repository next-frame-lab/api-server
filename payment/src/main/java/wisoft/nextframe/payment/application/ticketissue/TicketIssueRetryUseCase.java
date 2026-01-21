package wisoft.nextframe.payment.application.ticketissue;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.payment.application.payment.port.output.TicketingClient;
import wisoft.nextframe.payment.application.ticketissue.port.output.TicketIssueOutboxRepository;
import wisoft.nextframe.payment.domain.ReservationId;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketIssueRetryUseCase {

	private final TicketIssueOutboxRepository outboxRepository;
	private final TicketingClient ticketingClient;

	public void runOnce() {
		var now = LocalDateTime.now();
		var targets = outboxRepository.findReady(now, 50);

		log.debug("runOnce targets.size={}, now={}", targets.size(), now);

		for (var t : targets) {
			log.debug("runOnce processing reservationId={}", t.reservationId());
			try {
				var response = ticketingClient.issueTicket(ReservationId.of(t.reservationId()));
				outboxRepository.markSuccess(t.reservationId(), response.ticketId(), now);
			} catch (Exception e) {

				if (e instanceof io.github.resilience4j.circuitbreaker.CallNotPermittedException) {
					log.warn("티켓 발급 차단됨 (CIRCUIT_BREAKER_OPEN). reservationId={}", t.reservationId());
				} else {
					log.warn("티켓 발급 외부 호출 실패. reservationId={}, error={}", t.reservationId(), e.toString());
				}

				outboxRepository.failAndBackoff(t.reservationId(), e.toString(), now);
			}
		}
	}

	public boolean hasPending() {
		return outboxRepository.countPending() > 0;
	}
}
