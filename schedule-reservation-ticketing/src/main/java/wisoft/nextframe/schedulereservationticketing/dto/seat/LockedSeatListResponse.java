package wisoft.nextframe.schedulereservationticketing.dto.seat;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class LockedSeatListResponse {

	private List<LockedSeatResponse> seats;
}
