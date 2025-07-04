package wisoft.nextframe.performance;

import wisoft.nextframe.user.User;

public class PerformanceDetail {

	private final PerformanceGenre genre;
	private final PerformanceType type;
	private final boolean adultOnly;

	public PerformanceDetail(PerformanceGenre genre, PerformanceType type, boolean adultOnly) {
		this.genre = genre;
		this.type = type;
		this.adultOnly = adultOnly;
	}

	public boolean isReservableBy(User user) {
		return !adultOnly || user.isAdult();
	}
}
