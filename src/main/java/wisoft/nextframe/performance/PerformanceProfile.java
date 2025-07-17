package wisoft.nextframe.performance;

import wisoft.nextframe.user.User;

public class PerformanceProfile {

	private final String name;
	private final String description;
	private final String image;
	private final PerformanceGenre genre;
	private final PerformanceType type;
	private final boolean adultOnly;

	public PerformanceProfile(String name, String description, String image, PerformanceGenre genre, PerformanceType type,
		boolean adultOnly) {
		this.name = name;
		this.description = description;
		this.image = image;
		this.genre = genre;
		this.type = type;
		this.adultOnly = adultOnly;
	}

	public boolean isReservableBy(User user) {
		return !adultOnly || user.isAdult();
	}
}
