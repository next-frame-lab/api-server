package wisoft.nextframe.schedulereservationticketing.service.reservation;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationTimeoutScheduler {

	private final ReservationTimeoutCanceller reservationTimeoutCanceller;

	@Scheduled(fixedRate = 10_000)
	public void cancelExpiredReservations() {
		int cancelledCount = reservationTimeoutCanceller
			.cancelExpiredReservations(LocalDateTime.now());

		if (cancelledCount > 0) {
			log.info("만료 예약 취소 스케줄러: {}건 취소 완료", cancelledCount);
		}
	}
}
