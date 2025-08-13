package wisoft.nextframe.schedulereservationticketing.dto.performance;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SeatSectionPriceDto {

	private final String section;
	private final Integer price;
}
