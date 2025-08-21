package wisoft.nextframe.schedulereservationticketing.dto.reservation.response;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PerformanceInfoResponse {

	private final String name;
	private final LocalDate scheduleDate;
	private final LocalTime scheduleTime;
}
