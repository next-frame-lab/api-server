package wisoft.nextframe.schedulereservationticketing.dto.performancelist.reponse;

import org.springframework.data.domain.Page;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaginationResponse {

	private final int page;
	private final int size;
	private final long totalItems;
	private final int totalPages;
	private final boolean hasNext;
	private final boolean hasPrevious;

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
