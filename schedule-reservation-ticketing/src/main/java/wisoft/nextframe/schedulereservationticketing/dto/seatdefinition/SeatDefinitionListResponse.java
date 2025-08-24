package wisoft.nextframe.schedulereservationticketing.dto.seatdefinition;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SeatDefinitionListResponse {

	private final List<SeatDefinitionResponse> seats;
}
