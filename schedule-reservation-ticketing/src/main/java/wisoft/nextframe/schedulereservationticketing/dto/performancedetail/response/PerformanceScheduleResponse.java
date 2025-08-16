package wisoft.nextframe.schedulereservationticketing.dto.performancedetail.response;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PerformanceScheduleResponse {

	private final UUID id;
	private final String date;
	private final String time;
}
