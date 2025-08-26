package wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PerformanceListResponse {
    private final List<PerformanceResponse> performances;
    private final PaginationResponse pagination;
}