package wisoft.nextframe.schedulereservationticketing.dto.seat.seatstate;

import java.util.List;

import wisoft.nextframe.schedulereservationticketing.entity.seat.SeatState;

public record SeatStateListResponse(List<SeatStateResponse> seats) {

	public static SeatStateListResponse from(List<SeatState> seatStates) {
		final List<SeatStateResponse> seatStateResponseList = seatStates.stream()
			.map(SeatStateResponse::from)
			.toList();

		return new SeatStateListResponse(seatStateResponseList);
	}
}
