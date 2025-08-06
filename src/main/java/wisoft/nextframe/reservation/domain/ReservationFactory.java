package wisoft.nextframe.reservation.domain;

import java.util.Set;

import wisoft.nextframe.common.Money;
import wisoft.nextframe.schedule.domain.performance.Performance;
import wisoft.nextframe.schedule.domain.seat.Seat;
import wisoft.nextframe.user.domain.User;

public class ReservationFactory {

	public Reservation create(User user, Performance performance, Set<Seat> seats) {
		final Money totalPrice = performance.calculateTotalPrice(seats);
		return Reservation.create(user, performance, seats, totalPrice);
	}
}
