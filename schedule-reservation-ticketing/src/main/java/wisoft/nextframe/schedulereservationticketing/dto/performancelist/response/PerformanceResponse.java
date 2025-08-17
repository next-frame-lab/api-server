package wisoft.nextframe.schedulereservationticketing.dto.performancelist.response;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

import lombok.Getter;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceGenre;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceType;

@Getter
public class PerformanceResponse {

	private final UUID id;
	private final String name;
	private final String imageUrl;
	private final PerformanceType type;
	private final PerformanceGenre genre;
	private final String stadiumName;
	private final LocalDate startDate;
	private final LocalDate endDate;
	private final Boolean adultOnly;

	public PerformanceResponse(
		UUID id,
		String name,
		String imageUrl,
		PerformanceType type,
		PerformanceGenre genre,
		String stadiumName,
		Date startDate,
		Date endDate,
		Boolean adultOnly
	) {
		this.id = id;
		this.name = name;
		this.imageUrl = imageUrl;
		this.type = type;
		this.genre = genre;
		this.stadiumName = stadiumName;
		this.startDate = ((java.sql.Date) startDate).toLocalDate();
		this.endDate = ((java.sql.Date) endDate).toLocalDate();
		this.adultOnly = adultOnly;
	}
}
