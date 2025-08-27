package wisoft.nextframe.schedulereservationticketing.dto.reservation.response;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Builder;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;

@Builder
public record ReservationPerformanceResponse(String name, LocalDate scheduleDate, LocalTime scheduleTime) {

	public static ReservationPerformanceResponse from(Performance performance, Schedule schedule) {
		return ReservationPerformanceResponse.builder()
			.name(performance.getName())
			.scheduleDate(schedule.getPerformanceDatetime().toLocalDate())
			.scheduleTime(schedule.getPerformanceDatetime().toLocalTime())
			.build();
	}
}
