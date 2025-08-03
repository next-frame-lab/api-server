package wisoft.nextframe.domain.performance;

import java.time.Duration;

import lombok.Getter;
import wisoft.nextframe.domain.user.User;

@Getter
public class PerformanceProfile {

	private final String name;
	private final String description;
	private final Duration runningTime;
	private final String imageUrl;
	private final PerformanceGenre genre;
	private final PerformanceType type;
	private final boolean adultOnly;

	public PerformanceProfile(
		String name,
		String description,
		Duration runningTime,
		String imageUrl,
		PerformanceGenre genre,
		PerformanceType type,
		boolean adultOnly
	) {
		this.name = name;
		this.description = description;
		this.runningTime = runningTime;
		this.imageUrl = imageUrl;
		this.genre = genre;
		this.type = type;
		this.adultOnly = adultOnly;
	}

	public static PerformanceProfile of(
		String name,
		String description,
		Duration runningTime,
		String image,
		PerformanceGenre genre,
		PerformanceType type,
		boolean adultOnly
	) {
		return new PerformanceProfile(
			name,
			description,
			runningTime,
			image,
			genre,
			type,
			adultOnly);
	}

	public boolean isReservableBy(User user) {
		return !adultOnly || user.isAdult();
	}
}
