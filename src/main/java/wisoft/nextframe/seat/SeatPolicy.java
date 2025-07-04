package wisoft.nextframe.seat;

import java.util.Set;

import wisoft.nextframe.seat.exception.InvalidSeatForStadiumException;
import wisoft.nextframe.seat.exception.NoSeatSelectedException;
import wisoft.nextframe.seat.exception.SeatAlreadyLockedException;
import wisoft.nextframe.seat.exception.TooManySeatsSelectedException;
import wisoft.nextframe.stadium.Stadium;

public class SeatPolicy {

	public static void validateSeatsCount(Set<Seat> seats) {
		if (seats == null || seats.isEmpty()) {
			throw new NoSeatSelectedException();
		}

		if (seats.size() > 4) {
			throw new TooManySeatsSelectedException();
		}
	}

	public static void validateSeatsAreUnlocked(Set<Seat> seats) {
		if (seats.stream().anyMatch(Seat::isLocked)) {
			throw new SeatAlreadyLockedException();
		}
	}

	public static void validateSeatsBelongToStadium(Set<Seat> seats, Stadium stadium) {
		for (Seat seat : seats) {
			if (!stadium.hasSeat(seat)) {
				throw new InvalidSeatForStadiumException();
			}
		}
	}
}
