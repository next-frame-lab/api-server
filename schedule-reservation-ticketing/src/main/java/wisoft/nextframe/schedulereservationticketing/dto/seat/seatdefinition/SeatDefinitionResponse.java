package wisoft.nextframe.schedulereservationticketing.dto.seat.seatdefinition;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;

public record SeatDefinitionResponse(UUID id, String section, Integer row, Integer column) {

	public static SeatDefinitionResponse from(SeatDefinition seatDefinition) {
		return new SeatDefinitionResponse(
			seatDefinition.getId(),
			seatDefinition.getStadiumSection().getSection(),
			seatDefinition.getRowNo(),
			seatDefinition.getColumnNo()
		);
	}
}
