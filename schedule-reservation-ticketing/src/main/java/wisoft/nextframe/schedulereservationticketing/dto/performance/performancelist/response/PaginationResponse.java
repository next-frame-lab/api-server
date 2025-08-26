package wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response;

import org.springframework.data.domain.Page;

import lombok.Builder;

@Builder
public record PaginationResponse(int page, int size, long totalItems, int totalPages, boolean hasNext,
																 boolean hasPrevious) {

	public static <T> PaginationResponse from(Page<T> page) {
		return PaginationResponse.builder()
			.page(page.getNumber())
			.size(page.getSize())
			.totalItems(page.getTotalElements())
			.totalPages(page.getTotalPages())
			.hasNext(page.hasNext())
			.hasPrevious(page.hasPrevious())
			.build();
	}
}
