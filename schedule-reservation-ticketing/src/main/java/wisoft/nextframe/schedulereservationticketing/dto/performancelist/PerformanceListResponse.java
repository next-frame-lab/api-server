package wisoft.nextframe.schedulereservationticketing.dto.performancelist;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PerformanceListResponse {
    private final List<PerformanceSummaryDto> performances;
    private final PaginationDto pagination;
}