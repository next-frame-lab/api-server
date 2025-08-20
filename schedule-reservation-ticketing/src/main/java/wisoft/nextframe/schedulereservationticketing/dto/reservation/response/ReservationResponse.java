package wisoft.nextframe.schedulereservationticketing.dto.reservation.response;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ReservationResponse {

	private final UUID reservationId;
	private final PerformanceInfo performance;
	private final List<SeatInfo> seats;
	private final int totalAmount;
}
