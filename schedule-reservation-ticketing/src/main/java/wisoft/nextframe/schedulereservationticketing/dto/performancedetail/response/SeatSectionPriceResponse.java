package wisoft.nextframe.schedulereservationticketing.dto.performancedetail.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SeatSectionPriceResponse {

	private final String section;
	private final Integer price;
}
