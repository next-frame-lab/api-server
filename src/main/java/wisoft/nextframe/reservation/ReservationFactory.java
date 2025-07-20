package wisoft.nextframe.reservation;

import java.util.Set;

import wisoft.nextframe.common.Money;
import wisoft.nextframe.performance.Performance;
import wisoft.nextframe.seat.Seat;
import wisoft.nextframe.user.User;

public class ReservationFactory {

	public Reservation create(User user, Performance performance, Set<Seat> seats) {
		final Money totalPrice = performance.calculateTotalPrice(seats);
		return Reservation.create(user, performance, seats, totalPrice);
	}
}
