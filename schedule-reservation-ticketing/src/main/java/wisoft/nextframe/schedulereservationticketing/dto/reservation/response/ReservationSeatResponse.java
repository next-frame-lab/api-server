package wisoft.nextframe.schedulereservationticketing.dto.reservation.response;

import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;

public record ReservationSeatResponse(String section, int row, int column) {

	public static ReservationSeatResponse from(SeatDefinition seat) {
		return new ReservationSeatResponse(
			seat.getStadiumSection().getSection(),
			seat.getRowNo(),
			seat.getColumnNo()
		);
	}
}
