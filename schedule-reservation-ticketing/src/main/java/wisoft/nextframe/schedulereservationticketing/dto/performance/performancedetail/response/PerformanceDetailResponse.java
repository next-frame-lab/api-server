package wisoft.nextframe.schedulereservationticketing.dto.performance.performancedetail.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Builder;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceStatistic;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;

@Builder
public record PerformanceDetailResponse(
	UUID id,
	String imageUrl,
	String name,
	String type,
	String genre,
	BigDecimal averageStar,
	Integer runningTime,
	String description,
	Boolean adultOnly,
	LocalDateTime ticketOpenTime,
	LocalDateTime ticketCloseTime,
	StadiumResponse stadium,
	List<PerformanceScheduleResponse> performanceSchedules,
	List<SeatSectionPriceResponse> seatSectionPrices
) {

	public static PerformanceDetailResponse from(
		Performance performance,
		List<Schedule> schedules,
		List<SeatSectionPriceResponse> seatSectionPrices,
		PerformanceStatistic performanceStatistic
	) {
		final StadiumResponse stadiumResponse = schedules.stream()
			.findFirst()
			.map(schedule -> StadiumResponse.from(schedule.getStadium()))
			.orElseThrow(() -> new DomainException(ErrorCode.STADIUM_NOT_FOUND));

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
			.averageStar(performanceStatistic.getAverageStar())
			.runningTime((int)performance.getRunningTime().toMinutes())
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
