package wisoft.nextframe.policy;

import java.util.Set;

import wisoft.nextframe.seat.Seat;
import wisoft.nextframe.stadium.Stadium;

public class SeatPolicy {

	public static void validate(Set<Seat> seats, Stadium stadium) {
		validateSeatsCount(seats);
		validateSeatsAreReservable(seats);
		validateSeatsBelongToStadium(seats, stadium);
	}

	private static void validateSeatsCount(Set<Seat> seats) {
		if (seats == null || seats.isEmpty()) {
			throw new IllegalArgumentException("좌석은 최소 1개 이상 선택해야 합니다.");
		}

		if (seats.size() > 4) {
			throw new IllegalArgumentException("좌석은 최대 4개까지만 선택할 수 있습니다.");
		}
	}

	private static void validateSeatsAreReservable(Set<Seat> seats) {
		if (seats.stream().anyMatch(Seat::isLocked)) {
			throw new IllegalArgumentException("이미 예약된 좌석은 선택할 수 없습니다.");
		}
	}

	private static void validateSeatsBelongToStadium(Set<Seat> seats, Stadium stadium) {
		for (Seat seat : seats) {
			if (!stadium.hasSeat(seat)) {
				throw new IllegalArgumentException("해당 공연장의 좌석이 아닙니다.");
			}
		}
	}
}
