package wisoft.nextframe.schedulereservationticketing.dto.seat.seatdefinition;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Builder;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;

@Builder
public record SeatDefinitionListResponse(List<SeatDefinitionResponse> seats) {

	public static SeatDefinitionListResponse from(List<SeatDefinition> seatDefinitions) {
		List<SeatDefinitionResponse> seatDtoList = seatDefinitions.stream()
			.map(SeatDefinitionResponse::from)
			.collect(Collectors.toList());

		return SeatDefinitionListResponse.builder()
			.seats(seatDtoList)
			.build();
	}
}
