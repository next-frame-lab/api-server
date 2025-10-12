package wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response;

import java.util.List;

public record Top10PerformanceListResponse(
	List<PerformanceSummaryResponse> performances
) {
}
