package wisoft.nextframe.payment.application.payment.outbox;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Outbox 저장소의 공통 인터페이스
 */
public interface OutboxRepository<T extends OutboxTarget> {

	void upsertPending(UUID paymentId, UUID reservationId, String lastError, LocalDateTime now);

	List<T> findTargets(LocalDateTime now, int limit);

	void failAndBackoff(UUID reservationId, String lastError, LocalDateTime now);

	long countPending();
}
