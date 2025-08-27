package wisoft.nextframe.schedulereservationticketing.dto.reservation.response;

import java.time.LocalDate;
import java.time.LocalTime;

public record PerformanceInfoResponse(String name, LocalDate scheduleDate, LocalTime scheduleTime) {

}
