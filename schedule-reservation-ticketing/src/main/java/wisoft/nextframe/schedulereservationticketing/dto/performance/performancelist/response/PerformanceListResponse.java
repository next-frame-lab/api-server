package wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Builder;

@Builder
public record PerformanceListResponse(List<PerformanceSummaryResponse> performances, PaginationResponse pagination) {

	public static PerformanceListResponse from(Page<PerformanceSummaryResponse> performancePage) {
		return PerformanceListResponse.builder()
			.performances(performancePage.getContent())
			.pagination(PaginationResponse.from(performancePage))
			.build();
	}
}