package wisoft.nextframe.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

import wisoft.nextframe.common.Money;
import wisoft.nextframe.domain.performance.Performance;
import wisoft.nextframe.domain.performance.PerformanceGenre;
import wisoft.nextframe.domain.performance.PerformanceProfile;
import wisoft.nextframe.domain.performance.PerformanceType;
import wisoft.nextframe.domain.performance.ReservablePeriod;
import wisoft.nextframe.domain.performance.Schedule;
import wisoft.nextframe.domain.stadium.Stadium;

public class PerformanceFixture {

	private static final String DEFAULT_NAME = "공연";
	private static final String DEFAULT_DESCRIPTION = "유명한 가수의 공연입니다.";
	private static final String DEFAULT_IMAGE = "https://example.com/images/performance.jpg";
	private static final PerformanceGenre DEFAULT_GENRE = PerformanceGenre.CLASSIC;
	private static final PerformanceType DEFAULT_TYPE = PerformanceType.HORROR;
	private static final Map<String, Money> DEFAULT_SECTION_PRICE = Map.of(
		"A", Money.of(120_000),
		"B", Money.of(100_000),
		"C", Money.of(80_000)
	);
	private static final boolean NOT_ADULT_ONLY = false;
	private static final boolean ADULT_ONLY = true;

	private static final Schedule DEFAULT_SCHEDULE = new Schedule(
		LocalDateTime.now().plusWeeks(3),
		Duration.ofHours(3)
	);

	private static final ReservablePeriod OPEN_RESERVATION = new ReservablePeriod(
		LocalDateTime.now().minusHours(1),
		LocalDateTime.now().plusHours(1)
	);

	private static final ReservablePeriod CLOSED_RESERVATION = new ReservablePeriod(
		LocalDateTime.now().minusDays(4),
		LocalDateTime.now().minusDays(3)
	);

	private static final Stadium DEFAULT_STADIUM = StadiumFixture.defaultStadium();

	private PerformanceFixture() {
	}

	public static Performance defaultPerformance() {
		return createPerformance(NOT_ADULT_ONLY, DEFAULT_SCHEDULE, OPEN_RESERVATION);
	}

	public static Performance withSectionPrice(Map<String, Money> sectionPrice) {
		return Performance.create(defaultProfile(NOT_ADULT_ONLY), DEFAULT_SCHEDULE, sectionPrice, DEFAULT_STADIUM,
			OPEN_RESERVATION);
	}

	public static Performance adultOnlyPerformance() {
		return createPerformance(ADULT_ONLY, DEFAULT_SCHEDULE, OPEN_RESERVATION);
	}

	public static Performance alreadyStartedPerformance() {
		Schedule started = new Schedule(LocalDateTime.now().minusHours(2), Duration.ofHours(3));
		return createPerformance(NOT_ADULT_ONLY, started, OPEN_RESERVATION);
	}

	public static Performance reservationClosedPerformance() {
		return createPerformance(NOT_ADULT_ONLY, DEFAULT_SCHEDULE, CLOSED_RESERVATION);
	}

	public static Performance reservationOpenPerformance() {
		return createPerformance(NOT_ADULT_ONLY, DEFAULT_SCHEDULE, OPEN_RESERVATION);
	}

	private static Performance createPerformance(boolean adultOnly, Schedule schedule, ReservablePeriod period) {
		return Performance.create(defaultProfile(adultOnly), schedule, DEFAULT_SECTION_PRICE, DEFAULT_STADIUM, period);
	}

	private static PerformanceProfile defaultProfile(boolean adultOnly) {
		return new PerformanceProfile(
			DEFAULT_NAME,
			DEFAULT_DESCRIPTION,
			DEFAULT_IMAGE,
			DEFAULT_GENRE,
			DEFAULT_TYPE,
			adultOnly
		);
	}

}
