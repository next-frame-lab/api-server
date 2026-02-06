package wisoft.nextframe.payment.application.payment.outbox;

import java.time.LocalDateTime;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * Outbox 재시도 로직의 공통 추상 클래스
 */
@Slf4j
public abstract class AbstractOutboxRetryUseCase<T extends OutboxTarget> {

	private static final int BATCH_SIZE = 50;
	private static final int MAX_ERROR_LENGTH = 500;

	protected abstract OutboxRepository<T> getRepository();

	protected abstract void executeExternalCall(T target, LocalDateTime now);

	protected abstract String getLogPrefix();

	public void runOnce() {
		LocalDateTime now = LocalDateTime.now();
		List<T> targets = getRepository().findTargets(now, BATCH_SIZE);

		log.info("{} runOnce now={}, batchSize={}, targetsSize={}",
			getLogPrefix(), now, BATCH_SIZE, targets.size());

		for (T target : targets) {
			processOne(target, now);
		}
	}

	private void processOne(T target, LocalDateTime now) {
		try {
			executeExternalCall(target, now);
		} catch (Exception e) {
			String lastError = trimError(e);
			log.debug("{} 재시도 실패로 상태 갱신. reservationId={}, lastError={}",
				getLogPrefix(), target.reservationId(), lastError);
			getRepository().failAndBackoff(target.reservationId(), lastError, now);
		}
	}

	public boolean hasPending() {
		return getRepository().countPending() > 0;
	}

	protected String trimError(Exception e) {
		String msg = e.toString();
		if (msg.length() <= MAX_ERROR_LENGTH) {
			return msg;
		}
		return msg.substring(0, MAX_ERROR_LENGTH);
	}
}
