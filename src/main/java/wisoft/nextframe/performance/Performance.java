package wisoft.nextframe.performance;

import java.time.Duration;
import java.time.LocalDateTime;

import lombok.Getter;
import wisoft.nextframe.stadium.Stadium;
import wisoft.nextframe.user.User;

@Getter
public class Performance {

	private final String title;
	private final String description;
	private final String image;
	private final PerformanceCategory category;
	private final PerformanceType type;
	private final boolean adultOnly;
	private final LocalDateTime startTime;
	private final Duration runningTime;
	private final int basePrice;
	private final Stadium stadium;
	private final LocalDateTime reservationStartTime;
	private final LocalDateTime reservationEndTime;

	private Performance(String title, String description, String image, PerformanceCategory category,
		PerformanceType type, boolean adultOnly, LocalDateTime startTime, Duration runningTime, int basePrice,
		Stadium stadium,
		LocalDateTime reservationStartTime, LocalDateTime reservationEndTime) {
		this.title = title;
		this.description = description;
		this.image = image;
		this.category = category;
		this.type = type;
		this.adultOnly = adultOnly;
		this.startTime = startTime;
		this.runningTime = runningTime;
		this.basePrice = basePrice;
		this.stadium = stadium;
		this.reservationStartTime = reservationStartTime;
		this.reservationEndTime = reservationEndTime;
	}

	public static Performance create(String title, String description, String image, PerformanceCategory category,
		PerformanceType type, boolean adultOnly, LocalDateTime startTime, Duration runningTime, int basePrice,
		Stadium stadium,
		LocalDateTime reservationStartTime, LocalDateTime reservationEndTime) {
		return new Performance(title, description, image, category, type, adultOnly, startTime, runningTime, basePrice,
			stadium, reservationStartTime, reservationEndTime);
	}

	public boolean isReservableBy(User user) {
		return !adultOnly || user.isAdult();
	}

	public boolean isStarted() {
		return startTime.isBefore(LocalDateTime.now().withNano(0));
	}

	public boolean isReservableNow() {
		final LocalDateTime now = LocalDateTime.now();
		return !now.isBefore(reservationStartTime) && !now.isAfter(reservationEndTime);
	}
}
