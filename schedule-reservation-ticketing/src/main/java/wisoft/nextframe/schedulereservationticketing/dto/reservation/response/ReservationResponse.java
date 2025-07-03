package wisoft.nextframe.schedulereservationticketing.dto.reservation.response;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.Reservation;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;

@Builder
public record ReservationResponse(
	UUID reservationId,
	ReservationPerformanceResponse performance,
	List<ReservationSeatResponse> seats,
	int totalAmount
) {

	public static ReservationResponse from(
		Reservation reservation,
		Performance performance,
		Schedule schedule,
		List<SeatDefinition> seats
	) {
		final ReservationPerformanceResponse performanceResponse
			= ReservationPerformanceResponse.from(performance, schedule);
		final List<ReservationSeatResponse> reservationSeatList = seats.stream()
			.map(ReservationSeatResponse::from)
			.toList();

		return new ReservationResponse(
			reservation.getId(),
			performanceResponse,
			reservationSeatList,
			reservation.getTotalPrice()
		);
	}
}
