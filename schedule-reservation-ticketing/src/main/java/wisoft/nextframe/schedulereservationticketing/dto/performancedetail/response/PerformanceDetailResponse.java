package wisoft.nextframe.schedulereservationticketing.dto.performancedetail.response;

import java.time.LocalDateTime;
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
	private final LocalDateTime ticketOpenTime;
	private final LocalDateTime ticketCloseTime;
	private final StadiumResponse stadium;
	private final List<PerformanceScheduleResponse> performanceSchedules;
	private final List<SeatSectionPriceResponse> seatSectionPrices;
}
