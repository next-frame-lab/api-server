package wisoft.nextframe.schedulereservationticketing.dto.seat.seatstate;

import java.util.List;

import lombok.Builder;
import wisoft.nextframe.schedulereservationticketing.entity.seat.SeatState;

@Builder
public record SeatStateListResponse(List<SeatStateResponse> seats) {

	public static SeatStateListResponse from(List<SeatState> seatStates) {
		final List<SeatStateResponse> seatStateResponses = seatStates.stream()
			.map(SeatStateResponse::from)
			.toList();

		return SeatStateListResponse.builder()
			.seats(seatStateResponses)
			.build();
	}
}
