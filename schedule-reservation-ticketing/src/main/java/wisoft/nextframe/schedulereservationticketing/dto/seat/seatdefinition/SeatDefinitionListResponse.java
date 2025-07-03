package wisoft.nextframe.schedulereservationticketing.dto.seat.seatdefinition;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Builder;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;

public record SeatDefinitionListResponse(List<SeatDefinitionResponse> seats) {

	public static SeatDefinitionListResponse from(List<SeatDefinition> seatDefinitions) {
		List<SeatDefinitionResponse> seatDefinitionResponseList = seatDefinitions.stream()
			.map(SeatDefinitionResponse::from)
			.collect(Collectors.toList());

		return new SeatDefinitionListResponse(seatDefinitionResponseList);
	}
}
