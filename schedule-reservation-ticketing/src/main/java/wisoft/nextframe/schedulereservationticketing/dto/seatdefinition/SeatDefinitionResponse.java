package wisoft.nextframe.schedulereservationticketing.dto.seatdefinition;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SeatDefinitionResponse {

	private final UUID id;
	private final String section;
	private final Integer row;
	private final Integer column;
}
