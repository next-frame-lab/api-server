package wisoft.nextframe.schedulereservationticketing.dto.seatstate;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class LockedSeatResponse {

	private UUID id;
	private boolean isLocked;
}
