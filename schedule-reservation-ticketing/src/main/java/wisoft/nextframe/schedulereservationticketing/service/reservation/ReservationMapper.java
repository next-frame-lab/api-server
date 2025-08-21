package wisoft.nextframe.schedulereservationticketing.service.reservation;

import java.util.List;

import org.springframework.stereotype.Component;

import wisoft.nextframe.schedulereservationticketing.dto.reservation.response.PerformanceInfoResponse;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.response.ReservationResponse;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.response.SeatInfoResponse;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.Reservation;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;

@Component
public class ReservationMapper {

	/**
	 * 예매 관련 엔티티들을 ReservationResponse DTO로 변환합니다.
	 * @param reservation 예매 엔티티
	 * @param performance 공연 엔티티
	 * @param schedule 스케줄 엔티티
	 * @param seats 좌석 정의 엔티티 목록
	 * @return ReservationResponse DTO
	 */
	public ReservationResponse toResponse(
		Reservation reservation,
		Performance performance,
		Schedule schedule,
		List<SeatDefinition> seats
	) {
		final PerformanceInfoResponse performanceInfoResponse = new PerformanceInfoResponse(
			performance.getName(),
			schedule.getPerformanceDatetime().toLocalDate(),
			schedule.getPerformanceDatetime().toLocalTime()
		);

		final List<SeatInfoResponse> seatInfoResponses = seats.stream()
			.map(seat -> new SeatInfoResponse(
				seat.getStadiumSection().getSection(),
				seat.getRowNo(),
				seat.getColumnNo()
			))
			.toList();

		return new ReservationResponse(
			reservation.getId(),
			performanceInfoResponse,
			seatInfoResponses,
			reservation.getTotalPrice()
		);
	}
}
