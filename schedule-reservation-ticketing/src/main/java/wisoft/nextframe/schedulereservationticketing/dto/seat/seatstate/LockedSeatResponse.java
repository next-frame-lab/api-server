package wisoft.nextframe.schedulereservationticketing.dto.seat.seatstate;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
public record LockedSeatResponse(
	UUID id,
	@JsonProperty("isLocked")
	boolean isLocked
) {
}
