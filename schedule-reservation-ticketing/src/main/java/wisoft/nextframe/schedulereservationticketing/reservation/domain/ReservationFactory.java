package wisoft.nextframe.schedulereservationticketing.reservation.domain;

import java.util.Set;

import wisoft.nextframe.schedulereservationticketing.common.Money;
import wisoft.nextframe.schedulereservationticketing.schedule.domain.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.schedule.domain.seat.Seat;
import wisoft.nextframe.schedulereservationticketing.user.domain.User;

public class ReservationFactory {

	public Reservation create(User user, Performance performance, Set<Seat> seats) {
		final Money totalPrice = performance.calculateTotalPrice(seats);
		return Reservation.create(user, performance, seats, totalPrice);
	}
}
