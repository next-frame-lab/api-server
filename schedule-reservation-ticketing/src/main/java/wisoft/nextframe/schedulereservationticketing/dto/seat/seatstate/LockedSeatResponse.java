package wisoft.nextframe.schedulereservationticketing.dto.seat.seatstate;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
public class LockedSeatResponse {

	@Getter
	private UUID id;

	private boolean isLocked;

	@JsonProperty("isLocked")
	public boolean isLocked() {
		return this.isLocked;
	}
}
