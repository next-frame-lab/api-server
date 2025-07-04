package wisoft.nextframe.util;

import java.time.Duration;
import java.time.LocalDateTime;

import wisoft.nextframe.performance.Performance;
import wisoft.nextframe.performance.PerformanceCategory;
import wisoft.nextframe.performance.PerformanceType;
import wisoft.nextframe.stadium.Stadium;

public class PerformanceFixture {

	private static final String DEFAULT_NAME = "공연";
	private static final String DEFAULT_DESCRIPTION = "명불허전 가수의 공연입니다.";
	private static final String DEFAULT_IMAGE = "performance.jpg";
	private static final PerformanceCategory DEFAULT_CATEGORY = PerformanceCategory.CLASSIC;
	private static final PerformanceType DEFAULT_TYPE = PerformanceType.HORROR;
	private static final boolean DEFAULT_ADULT_ONLY = false;
	private static final LocalDateTime DEFAULT_SHOW_TIME = LocalDateTime.now().plusWeeks(3);
	private static final Duration DEFAULT_RUNNING_TIME = Duration.ofHours(3);
	private static final int DEFAULT_BASE_PRICE = 130_000;
	private static final Stadium DEFAULT_STADIUM = StadiumFixture.defaultStadium();
	private static final LocalDateTime DEFAULT_RESERVATION_OPEN_TIME = LocalDateTime.now().minusHours(1);
	private static final LocalDateTime DEFAULT_RESERVATION_CLOSE_TIME = LocalDateTime.now().plusHours(1);

	private PerformanceFixture() {
	}

	public static Performance defaultPerformance() {
		return create(DEFAULT_NAME, DEFAULT_DESCRIPTION, DEFAULT_IMAGE, DEFAULT_CATEGORY, DEFAULT_TYPE, DEFAULT_ADULT_ONLY,
			DEFAULT_SHOW_TIME, DEFAULT_RUNNING_TIME, DEFAULT_BASE_PRICE, DEFAULT_STADIUM,
			DEFAULT_RESERVATION_OPEN_TIME, DEFAULT_RESERVATION_CLOSE_TIME);
	}

	public static Performance createWith(int price, Stadium stadium) {
		return create(DEFAULT_NAME, DEFAULT_DESCRIPTION, DEFAULT_IMAGE, DEFAULT_CATEGORY, DEFAULT_TYPE, DEFAULT_ADULT_ONLY,
			DEFAULT_SHOW_TIME, DEFAULT_RUNNING_TIME, price, stadium,
			DEFAULT_RESERVATION_OPEN_TIME, DEFAULT_RESERVATION_CLOSE_TIME);
	}

	public static Performance createWith(LocalDateTime reservationStartTime, LocalDateTime reservationEndTime) {
		return create(DEFAULT_NAME, DEFAULT_DESCRIPTION, DEFAULT_IMAGE, DEFAULT_CATEGORY, DEFAULT_TYPE, DEFAULT_ADULT_ONLY,
			DEFAULT_SHOW_TIME, DEFAULT_RUNNING_TIME, DEFAULT_BASE_PRICE, DEFAULT_STADIUM,
			reservationStartTime, reservationEndTime);
	}

	public static Performance adultOnly() {
		return create(DEFAULT_NAME, DEFAULT_DESCRIPTION, DEFAULT_IMAGE, DEFAULT_CATEGORY, DEFAULT_TYPE, true,
			DEFAULT_SHOW_TIME, DEFAULT_RUNNING_TIME, DEFAULT_BASE_PRICE, DEFAULT_STADIUM,
			DEFAULT_RESERVATION_OPEN_TIME, DEFAULT_RESERVATION_CLOSE_TIME);
	}

	public static Performance started() {
		return create(DEFAULT_NAME, DEFAULT_DESCRIPTION, DEFAULT_IMAGE, DEFAULT_CATEGORY, DEFAULT_TYPE, DEFAULT_ADULT_ONLY,
			LocalDateTime.now().minusMinutes(1), DEFAULT_RUNNING_TIME, DEFAULT_BASE_PRICE,
			DEFAULT_STADIUM, DEFAULT_RESERVATION_OPEN_TIME, DEFAULT_RESERVATION_CLOSE_TIME);
	}

	private static Performance create(String title, String description, String image, PerformanceCategory category,
		PerformanceType type, boolean adultOnly, LocalDateTime showTime, Duration runningTime, int basePrice,
		Stadium stadium,
		LocalDateTime reservationStartTime, LocalDateTime reservationEndTime) {
		return Performance.create(title, description, image, category, type, adultOnly, showTime, runningTime, basePrice,
			stadium,
			reservationStartTime, reservationEndTime);
	}
}
