package wisoft.nextframe.schedulereservationticketing.dto.reservation.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SeatInfo {

	private final String section;
	private final int row;
	private final int column;
}
