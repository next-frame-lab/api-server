package wisoft.nextframe.schedulereservationticketing.dto.performance.response;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PerformanceScheduleResponseDto {

	private final UUID id;
	private final String date;
	private final String time;
}
