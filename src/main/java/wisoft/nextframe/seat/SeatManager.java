package wisoft.nextframe.seat;

import java.util.Set;

public class SeatManager {

	public void lockSeats(Set<Seat> seats) {
		seats.forEach(Seat::lock);
	}

	public void unlockSeats(Set<Seat> seats) {
		seats.forEach(Seat::unlock);
	}
}
