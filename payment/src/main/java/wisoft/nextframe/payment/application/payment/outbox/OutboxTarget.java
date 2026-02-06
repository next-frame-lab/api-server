package wisoft.nextframe.payment.application.payment.outbox;

import java.util.UUID;

/**
 * Outbox 재시도 대상의 공통 인터페이스
 */
public interface OutboxTarget {
	UUID reservationId();
	UUID paymentId();
}
