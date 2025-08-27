package wisoft.nextframe.schedulereservationticketing.dto.performance.performancedetail.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityNotFoundException;
import lombok.Builder;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;

@Builder
public record PerformanceDetailResponse(UUID id, String imageUrl, String name, String type, String genre,
																				Double averageStar, Integer runningTime, String description, Boolean adultOnly,
																				LocalDateTime ticketOpenTime, LocalDateTime ticketCloseTime,
																				StadiumResponse stadium, List<PerformanceScheduleResponse> performanceSchedules,
																				List<SeatSectionPriceResponse> seatSectionPrices) {

	// todo: 공연 별점 기능 개발 후 변경할 예정
	public static final Double DEFAULT_AVERAGE_STAR = 4.8;

	public static PerformanceDetailResponse from(
		Performance performance,
		List<Schedule> schedules,
		List<SeatSectionPriceResponse> seatSectionPrices
	) {
		final StadiumResponse stadiumResponse = schedules.stream()
			.findFirst()
			.map(schedule -> StadiumResponse.from(schedule.getStadium()))
			.orElseThrow(() -> new EntityNotFoundException("해당 공연장을 찾을 수 없습니다."));

		final List<PerformanceScheduleResponse> performanceScheduleResponses = schedules.stream()
			.map(PerformanceScheduleResponse::from)
			.toList();

		final LocalDateTime ticketOpenTime = schedules.getFirst().getTicketOpenTime();
		final LocalDateTime ticketCloseTime = schedules.getFirst().getTicketCloseTime();

		return PerformanceDetailResponse.builder()
			.id(performance.getId())
			.imageUrl(performance.getImageUrl())
			.name(performance.getName())
			.type(performance.getType().name())
			.genre(performance.getGenre().name())
			.averageStar(DEFAULT_AVERAGE_STAR)
			.runningTime((int) performance.getRunningTime().toMinutes())
			.description(performance.getDescription())
			.adultOnly(performance.getAdultOnly())
			.ticketOpenTime(ticketOpenTime)
			.ticketCloseTime(ticketCloseTime)
			.stadium(stadiumResponse)
			.performanceSchedules(performanceScheduleResponses)
			.seatSectionPrices(seatSectionPrices)
			.build();
	}
}
