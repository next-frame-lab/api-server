package wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceGenre;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceType;

public record PerformanceSummaryResponse(
	UUID id,
	String name,
	String imageUrl,
	PerformanceType type,
	PerformanceGenre genre,
	String stadiumName,
	LocalDate startDate,
	LocalDate endDate,
	Boolean adultOnly
) {

	public PerformanceSummaryResponse(
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
		this(id,
			name,
			imageUrl,
			type,
			genre,
			stadiumName,
			((java.sql.Date) startDate).toLocalDate(),
			((java.sql.Date) endDate).toLocalDate(),
			adultOnly
		);
	}
}
