package wisoft.nextframe.schedulereservationticketing.dto.performance.performancedetail.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Builder;

@Builder
public record PerformanceDetailResponse(UUID id, String imageUrl, String name, String type, String genre,
																				Double averageStar, Integer runningTime, String description, Boolean adultOnly,
																				LocalDateTime ticketOpenTime, LocalDateTime ticketCloseTime,
																				StadiumResponse stadium, List<PerformanceScheduleResponse> performanceSchedules,
																				List<SeatSectionPriceResponse> seatSectionPrices) {
}
