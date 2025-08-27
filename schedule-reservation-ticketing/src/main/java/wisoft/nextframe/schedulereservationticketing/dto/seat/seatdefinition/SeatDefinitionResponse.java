package wisoft.nextframe.schedulereservationticketing.dto.seat.seatdefinition;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;

@Builder
public record SeatDefinitionResponse(UUID id, String section, Integer row, Integer column) {

	public static SeatDefinitionResponse from(SeatDefinition seatDefinition) {
		return SeatDefinitionResponse.builder()
			.id(seatDefinition.getId())
			.section(seatDefinition.getStadiumSection().getSection())
			.row(seatDefinition.getRowNo())
			.column(seatDefinition.getColumnNo())
			.build();
	}
}
