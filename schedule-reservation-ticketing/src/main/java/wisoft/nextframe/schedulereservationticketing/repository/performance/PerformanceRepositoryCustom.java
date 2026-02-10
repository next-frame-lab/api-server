package wisoft.nextframe.schedulereservationticketing.repository.performance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PerformanceSummaryResponse;

public interface PerformanceRepositoryCustom {

	Page<PerformanceSummaryResponse> findReservablePerformances(Pageable pageable);

	Page<PerformanceSummaryResponse> findTop10Performances(Pageable pageable);
}