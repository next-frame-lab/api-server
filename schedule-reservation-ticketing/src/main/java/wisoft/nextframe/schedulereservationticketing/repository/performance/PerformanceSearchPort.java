package wisoft.nextframe.schedulereservationticketing.repository.performance;

import org.springframework.data.domain.Page;

import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PerformanceSummaryResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.search.request.PerformanceSearchCondition;

public interface PerformanceSearchPort {

	Page<PerformanceSummaryResponse> search(PerformanceSearchCondition condition);
}
