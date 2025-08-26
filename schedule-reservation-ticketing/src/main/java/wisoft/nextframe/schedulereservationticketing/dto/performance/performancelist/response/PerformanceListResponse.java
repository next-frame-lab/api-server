package wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response;

import java.util.List;
import lombok.Builder;

@Builder
public record PerformanceListResponse(List<PerformanceSummaryResponse> performances, PaginationResponse pagination) {
}