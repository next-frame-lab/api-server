package wisoft.nextframe.schedulereservationticketing.dto.reservation.response;

import lombok.Builder;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;

@Builder
public record ReservationSeatResponse(String section, int row, int column) {

	public static ReservationSeatResponse from(SeatDefinition seat) {
		return ReservationSeatResponse.builder()
			.section(seat.getStadiumSection().getSection())
			.row(seat.getRowNo())
			.column(seat.getColumnNo())
			.build();
	}
}
