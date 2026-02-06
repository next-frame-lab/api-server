package wisoft.nextframe.payment.infra.payment.schedule;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.payment.application.payment.outbox.cancel.ReservationCancelRetryUseCase;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class ReservationCancelScheduler {

	private final ReservationCancelRetryUseCase retryUseCase;

	@Scheduled(fixedDelayString = "${reservation.cancel.retry.delay-ms}")
	public void retryReservationCancel() {
		if (!retryUseCase.hasPending()) {
			log.debug("reservation cancel retry skipped. no pending");
			return;
		}

		log.debug("reservation cancel retry start");
		retryUseCase.runOnce();
	}
}
