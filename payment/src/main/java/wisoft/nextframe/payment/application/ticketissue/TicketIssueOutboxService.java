package wisoft.nextframe.payment.application.ticketissue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.payment.application.payment.port.output.TicketingClient;
import wisoft.nextframe.payment.application.ticketissue.dto.TicketIssueResult;
import wisoft.nextframe.payment.application.ticketissue.port.output.TicketIssueOutboxRepository;
import wisoft.nextframe.payment.domain.ReservationId;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketIssueOutboxService {

	private final TicketingClient ticketingClient;
	private final TicketIssueOutboxRepository outboxRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void issueOrEnqueue(UUID paymentId, UUID reservationId) {
		log.debug("outboxRepository class={}", outboxRepository.getClass());

		LocalDateTime now = LocalDateTime.now();

		// 1) 먼저 outbox에 PENDING upsert (쓰기 선반영)
		outboxRepository.upsertPending(paymentId, reservationId, null, now);
		try {
			// 2) 외부 호출
			TicketIssueResult response = ticketingClient.issueTicket(ReservationId.of(reservationId));

			// 3) 성공 처리
			outboxRepository.markSuccess(reservationId, response.ticketId(), now);
		} catch (Exception e) {
			// 4) 실패면 lastError만 업데이트 (이미 PENDING row는 있음)
			outboxRepository.upsertPending(paymentId, reservationId, e.toString(), now);
		}
	}
}