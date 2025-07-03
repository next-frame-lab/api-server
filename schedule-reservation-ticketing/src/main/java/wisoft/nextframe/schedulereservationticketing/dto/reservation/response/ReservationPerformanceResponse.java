package wisoft.nextframe.schedulereservationticketing.dto.reservation.response;

import java.time.LocalDate;
import java.time.LocalTime;

import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;

public record ReservationPerformanceResponse(String name, LocalDate scheduleDate, LocalTime scheduleTime) {

	public static ReservationPerformanceResponse from(Performance performance, Schedule schedule) {
		return new ReservationPerformanceResponse(
			performance.getName(),
			schedule.getPerformanceDatetime().toLocalDate(),
			schedule.getPerformanceDatetime().toLocalTime()
		);
	}
}
