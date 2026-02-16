package wisoft.nextframe.schedulereservationticketing.dto.performance.search.request;

import org.springframework.data.domain.Pageable;

public record PerformanceSearchCondition(
	String keyword,
	PerformanceSearchSort sort,
	Pageable pageable
) {

	public static PerformanceSearchCondition of(
		final String keyword,
		final PerformanceSearchSort sort,
		final Pageable pageable
	) {
		final String normalizedKeyword = (keyword != null) ? keyword.strip() : null;
		final String effectiveKeyword = (normalizedKeyword != null && normalizedKeyword.isEmpty()) ? null : normalizedKeyword;
		final PerformanceSearchSort effectiveSort = (sort != null) ? sort : PerformanceSearchSort.LATEST;

		return new PerformanceSearchCondition(effectiveKeyword, effectiveSort, pageable);
	}
}
