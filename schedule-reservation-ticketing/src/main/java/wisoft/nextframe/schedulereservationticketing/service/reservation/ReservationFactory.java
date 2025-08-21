package wisoft.nextframe.schedulereservationticketing.service.reservation;

import java.util.List;

import org.springframework.stereotype.Component;

import wisoft.nextframe.schedulereservationticketing.entity.reservation.Reservation;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.ReservationSeat;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;

@Component
public class ReservationFactory {

	public Reservation create(User user, Schedule schedule, List<SeatDefinition> seats, int totalPrice) {
		// 1. Reservation 엔티티를 생성합니다.
		final Reservation reservation = Reservation.create(user, schedule, totalPrice);

		// 2. ReservationSeat 목록을 생성합니다.
		final List<ReservationSeat> reservationSeats = seats.stream()
			.map(seat -> ReservationSeat.builder().seatDefinition(seat).build())
			.toList();

		// 3. Reservation과 ReservationSeat의 부모 자식 관계를 설정합니다.
		reservation.addReservationSeats(reservationSeats);

		// 4. 완성된 reservation 엔티티를 반환합니다.
		return reservation;
	}
}
