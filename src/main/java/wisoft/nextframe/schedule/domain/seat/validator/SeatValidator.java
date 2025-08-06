package wisoft.nextframe.schedule.domain.seat.validator;

import java.util.Set;

import wisoft.nextframe.schedule.domain.seat.Seat;
import wisoft.nextframe.schedule.domain.seat.exception.InvalidSeatForStadiumException;
import wisoft.nextframe.schedule.domain.seat.exception.NoSeatSelectedException;
import wisoft.nextframe.schedule.domain.seat.exception.SeatAlreadyLockedException;
import wisoft.nextframe.schedule.domain.seat.exception.TooManySeatsSelectedException;
import wisoft.nextframe.schedule.domain.stadium.Stadium;

public class SeatValidator {

	private static final int MAX_SEAT_LIMIT = 4;

	public void validate(Set<Seat> seats, Stadium stadium) {
		validateSeatsCount(seats);
		validateNotLocked(seats);
		validateSeatsBelongToStadium(seats, stadium);
	}

	private void validateSeatsCount(Set<Seat> seats) {
		if (seats.isEmpty()) {
			throw new NoSeatSelectedException();
		}
		if (seats.size() > MAX_SEAT_LIMIT) {
			throw new TooManySeatsSelectedException();
		}
	}

	private void validateNotLocked(Set<Seat> seats) {
		if (seats.stream().anyMatch(Seat::isLocked)) {
			throw new SeatAlreadyLockedException();
		}
	}

	private void validateSeatsBelongToStadium(Set<Seat> seats, Stadium stadium) {
		for (Seat seat : seats) {
			if (!stadium.hasSeat(seat)) {
				throw new InvalidSeatForStadiumException();
			}
		}
	}
}
