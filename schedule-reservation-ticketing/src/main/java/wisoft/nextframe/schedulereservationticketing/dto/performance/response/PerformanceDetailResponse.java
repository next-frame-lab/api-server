package wisoft.nextframe.schedulereservationticketing.dto.performance.response;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PerformanceDetailResponse {

	private final UUID id;
	private final String imageUrl;
	private final String name;
	private final String type;
	private final String genre;
	private final Double averageStar;
	private final Integer runningTime;
	private final String description;
	private final Boolean adultOnly;
	private final StadiumResponseDto stadium;
	private final List<PerformanceScheduleResponseDto> performanceSchedules;
	private final List<SeatSectionPriceResponseDto> seatSectionPrices;
}
