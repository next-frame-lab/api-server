package wisoft.nextframe.schedulereservationticketing.dto.reservation.response;

import java.util.List;
import java.util.UUID;

public record ReservationResponse(UUID reservationId, PerformanceInfoResponse performance, List<SeatInfoResponse> seats,
																	int totalAmount) {
}
