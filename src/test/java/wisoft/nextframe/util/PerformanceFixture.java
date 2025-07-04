package wisoft.nextframe.util;

import java.time.Duration;
import java.time.LocalDateTime;

import wisoft.nextframe.performance.Performance;
import wisoft.nextframe.performance.PerformanceDetail;
import wisoft.nextframe.performance.PerformanceGenre;
import wisoft.nextframe.performance.PerformanceInfo;
import wisoft.nextframe.performance.PerformanceType;
import wisoft.nextframe.performance.ReservationWindow;
import wisoft.nextframe.performance.Schedule;
import wisoft.nextframe.stadium.Stadium;

public class PerformanceFixture {

	private static final PerformanceInfo DEFAULT_INFO = new PerformanceInfo(
		"공연",
		"명불허전 가수의 공연입니다.",
		"performance.jpg"
	);
	private static final PerformanceDetail DEFAULT_DETAIL = new PerformanceDetail(
		PerformanceGenre.CLASSIC,
		PerformanceType.HORROR,
		false
	);
	private static final Schedule DEFAULT_SCHEDULE = new Schedule(
		LocalDateTime.now().plusWeeks(3),
		Duration.ofHours(3)
	);
	private static final ReservationWindow DEFAULT_RESERVATION_WINDOW = new ReservationWindow(
		LocalDateTime.now().minusHours(1),
		LocalDateTime.now().plusHours(1)
	);
	private static final int DEFAULT_BASE_PRICE = 130_000;
	private static final Stadium DEFAULT_STADIUM = StadiumFixture.defaultStadium();

	private PerformanceFixture() {
	}

	public static Performance defaultPerformance() {
		return create(DEFAULT_INFO, DEFAULT_DETAIL,
			DEFAULT_SCHEDULE, DEFAULT_BASE_PRICE, DEFAULT_STADIUM,
			DEFAULT_RESERVATION_WINDOW);
	}

	public static Performance createWith(int price, Stadium stadium) {
		return create(DEFAULT_INFO, DEFAULT_DETAIL,
			DEFAULT_SCHEDULE, price, stadium,
			DEFAULT_RESERVATION_WINDOW);
	}

	public static Performance createWith(LocalDateTime reservationStartTime, LocalDateTime reservationEndTime) {
		ReservationWindow reservationWindow = new ReservationWindow(
			reservationStartTime,
			reservationEndTime
		);
		return create(DEFAULT_INFO, DEFAULT_DETAIL,
			DEFAULT_SCHEDULE, DEFAULT_BASE_PRICE, DEFAULT_STADIUM,
			reservationWindow);
	}

	public static Performance adultOnly() {
		PerformanceDetail detail = new PerformanceDetail(
			PerformanceGenre.CLASSIC,
			PerformanceType.HORROR,
			true
		);
		return create(DEFAULT_INFO, detail,
			DEFAULT_SCHEDULE, DEFAULT_BASE_PRICE, DEFAULT_STADIUM,
			DEFAULT_RESERVATION_WINDOW);
	}

	public static Performance started() {
		Schedule schedule = new Schedule(
			LocalDateTime.now().minusHours(2),
			Duration.ofHours(3)
		);
		return create(DEFAULT_INFO, DEFAULT_DETAIL, schedule, DEFAULT_BASE_PRICE, DEFAULT_STADIUM,
			DEFAULT_RESERVATION_WINDOW);
	}

	public static Performance notYetOpened() {
		ReservationWindow reservationWindow = new ReservationWindow(
			LocalDateTime.now().plusHours(1),
			LocalDateTime.now().plusHours(5)
		);
		return create(DEFAULT_INFO, DEFAULT_DETAIL, DEFAULT_SCHEDULE, DEFAULT_BASE_PRICE, DEFAULT_STADIUM,
			reservationWindow);
	}

	public static Performance ended() {
		ReservationWindow reservationWindow = new ReservationWindow(
			LocalDateTime.now().minusDays(4),
			LocalDateTime.now().minusDays(3)
		);
		return create(DEFAULT_INFO, DEFAULT_DETAIL, DEFAULT_SCHEDULE, DEFAULT_BASE_PRICE, DEFAULT_STADIUM,
			reservationWindow);
	}

	private static Performance create(
		PerformanceInfo info,
		PerformanceDetail detail,
		Schedule schedule,
		int basePrice,
		Stadium stadium,
		ReservationWindow reservationWindow) {
		return Performance.create(info, detail, schedule, basePrice, stadium, reservationWindow);
	}
}
