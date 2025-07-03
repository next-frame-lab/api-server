package wisoft.nextframe.schedulereservationticketing.dto.performance.performancedetail.response;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

import lombok.Builder;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;

@Builder
public record PerformanceScheduleResponse(UUID id, String date, String time) {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

	public static PerformanceScheduleResponse from(Schedule schedule) {
		return PerformanceScheduleResponse.builder()
			.id(schedule.getId())
			.date(schedule.getPerformanceDatetime().format(DATE_FORMATTER))
			.time(schedule.getPerformanceDatetime().format(TIME_FORMATTER))
			.build();
	}
}
