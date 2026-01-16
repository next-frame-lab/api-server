package wisoft.nextframe.payment.infra.ticketissue.adaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.payment.application.ticketissue.dto.TicketIssueOutboxRow;
import wisoft.nextframe.payment.application.ticketissue.port.output.TicketIssueOutboxRepository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TicketIssueOutboxRepositoryImpl implements TicketIssueOutboxRepository {

	private final JpaTicketIssueOutboxRepository jpa;

	@Override
	@Transactional
	public void upsertPending(UUID paymentId, UUID reservationId, String lastError, LocalDateTime now) {
		log.info("UPSERT_PENDING called paymentId={}, reservationId={}, now={}",
			paymentId, reservationId, now);

		var entity = jpa.findByReservationId(reservationId).orElseGet(() -> {
			var e = new TicketIssueOutboxEntity();
			e.setPaymentId(paymentId);
			e.setReservationId(reservationId);
			e.setStatus("PENDING");
			e.setRetryCount(0);
			e.setNextRetryAt(now);
			e.setCreatedAt(now);
			e.setUpdatedAt(now);
			return e;
		});

		// 이미 SUCCESS면 굳이 덮어쓰지 않는 게 안전
		if ("SUCCESS".equals(entity.getStatus())) {
			return;
		}

		entity.setPaymentId(paymentId);
		entity.setStatus("PENDING");
		entity.setLastError(lastError);
		entity.setUpdatedAt(now);

		// 첫 적재면 now, 재적재면 즉시 재시도 대신 조금 뒤로 보내도 됨
		if (entity.getRetryCount() == 0)
			entity.setNextRetryAt(now);

		jpa.save(entity);
	}

	@Override
	@Transactional
	public void markSuccess(UUID reservationId, UUID ticketId, LocalDateTime now) {
		var entity = jpa.findByReservationId(reservationId).orElse(null);
		if (entity == null)
			return;

		entity.setStatus("SUCCESS");
		entity.setTicketId(ticketId);
		entity.setUpdatedAt(now);
		jpa.save(entity);
	}

	@Override
	public List<TicketIssueOutboxRow> findReady(LocalDateTime now, int limit) {
		return jpa.findReadyToRetry(now, PageRequest.of(0, limit))
			.stream()
			.map(e -> new TicketIssueOutboxRow(
				e.getReservationId(),
				e.getPaymentId(),
				e.getTicketId(),
				e.getStatus(),
				e.getRetryCount(),
				e.getNextRetryAt()
			))
			.toList();
	}

	@Override
	@Transactional
	public void failAndBackoff(UUID reservationId, String lastError, LocalDateTime now) {
		var entity = jpa.findByReservationId(reservationId).orElse(null);
		if (entity == null)
			return;

		int nextRetry = entity.getRetryCount() + 1;
		entity.setRetryCount(nextRetry);
		entity.setLastError(lastError);
		entity.setUpdatedAt(now);

		// 백오프: 5s, 30s, 2m, 10m, 이후 FAILED
		if (nextRetry == 1)
			entity.setNextRetryAt(now.plusSeconds(5));
		else if (nextRetry == 2)
			entity.setNextRetryAt(now.plusSeconds(30));
		else if (nextRetry == 3)
			entity.setNextRetryAt(now.plusMinutes(2));
		else if (nextRetry == 4)
			entity.setNextRetryAt(now.plusMinutes(10));
		else
			entity.setStatus("FAILED");

		jpa.save(entity);
	}

	@Override
	public long countPending() {
		return jpa.countByStatus("PENDING");
	}
}
