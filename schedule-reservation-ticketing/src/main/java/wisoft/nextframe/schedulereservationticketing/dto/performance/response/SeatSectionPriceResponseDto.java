package wisoft.nextframe.schedulereservationticketing.dto.performance.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SeatSectionPriceResponseDto {

	private final String section;
	private final Integer price;
}
