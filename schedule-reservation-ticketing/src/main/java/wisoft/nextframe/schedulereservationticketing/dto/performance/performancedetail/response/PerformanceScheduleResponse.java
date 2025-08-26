package wisoft.nextframe.schedulereservationticketing.dto.performance.performancedetail.response;

import java.util.UUID;

import lombok.Builder;

@Builder
public record PerformanceScheduleResponse(UUID id, String date, String time) {

}
