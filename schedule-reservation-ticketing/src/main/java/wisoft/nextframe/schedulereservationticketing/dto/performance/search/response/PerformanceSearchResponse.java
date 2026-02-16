package wisoft.nextframe.schedulereservationticketing.dto.performance.search.response;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Builder;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PaginationResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PerformanceSummaryResponse;

@Builder
public record PerformanceSearchResponse(
	String keyword,
	List<PerformanceSummaryResponse> performances,
	PaginationResponse pagination
) {

	public static PerformanceSearchResponse from(
		final String keyword,
		final Page<PerformanceSummaryResponse> performancePage
	) {
		return PerformanceSearchResponse.builder()
			.keyword(keyword)
			.performances(performancePage.getContent())
			.pagination(PaginationResponse.from(performancePage))
			.build();
	}
}
