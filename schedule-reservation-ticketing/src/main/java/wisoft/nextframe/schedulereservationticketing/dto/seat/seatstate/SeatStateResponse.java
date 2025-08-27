package wisoft.nextframe.schedulereservationticketing.dto.seat.seatstate;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import wisoft.nextframe.schedulereservationticketing.entity.seat.SeatState;

@Builder
public record SeatStateResponse(
	UUID id,
	@JsonProperty("isLocked")
	boolean isLocked
) {

	public static SeatStateResponse from(SeatState seatState) {
		return new SeatStateResponse(
			seatState.getSeat().getId(),
			seatState.getIsLocked()
		);
	}
}
