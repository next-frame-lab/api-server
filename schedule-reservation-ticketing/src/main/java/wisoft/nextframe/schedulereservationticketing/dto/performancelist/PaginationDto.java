package wisoft.nextframe.schedulereservationticketing.dto.performancelist;

import org.springframework.data.domain.Page;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaginationDto {

	private final int page;
	private final int size;
	private final long totalItems;
	private final int totalPages;
	private final boolean hasNext;
	private final boolean hasPrevious;

	public static <T> PaginationDto from(Page<T> page) {
		return PaginationDto.builder()
			.page(page.getNumber())
			.size(page.getSize())
			.totalItems(page.getTotalElements())
			.totalPages(page.getTotalPages())
			.hasNext(page.hasNext())
			.hasPrevious(page.hasPrevious())
			.build();
	}
}
