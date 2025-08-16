package wisoft.nextframe.schedulereservationticketing.dto.performancelist;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import lombok.Getter;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceGenre;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceType;

@Getter
public class PerformanceSummaryDto {

	private final UUID id;
	private final String name;
	private final String imageUrl;
	private final PerformanceType type;
	private final PerformanceGenre genre;
	private final String stadiumName;
	private final LocalDate startDate;
	private final LocalDate endDate;
	private final Boolean adultOnly;

	// JPQL의 'SELECT NEW' 구문에서 사용할 생성자
	public PerformanceSummaryDto(UUID id, String name, String imageUrl, PerformanceType type,
		PerformanceGenre genre, String stadiumName,
		// 파라미터 타입을 java.util.Date로 변경
		Date startDate, Date endDate, Boolean adultOnly) {
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
