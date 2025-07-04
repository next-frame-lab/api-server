package wisoft.nextframe.util;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import wisoft.nextframe.seat.Seat;

public class SeatFixture {

	private static final boolean DEFAULT_LOCKED = false;

	private SeatFixture() {
	}

	public static Seat available(String section, String row, int column) {
		return Seat.create(section, row, column);
	}

	public static Seat locked(String section, String row, int column) {
		Seat seat = Seat.create(section, row, column);
		seat.lock();
		return seat;
	}

	public static Set<Seat> availableSeats(String section, String row, int startColumn, int count) {
		return IntStream.range(startColumn, startColumn + count)
			.mapToObj(col -> available(section, row, col))
			.collect(Collectors.toSet());
	}

	public static Set<Seat> none() {
		return Set.of();
	}

	public static Set<Seat> exceedingLimitSeats() {
		return availableSeats("일반", "A", 1, 5);
	}

	public static Set<Seat> lockedSeats(String section, String row, int startColumn, int count) {
		return IntStream.range(startColumn, startColumn + count)
			.mapToObj(col -> locked(section, row, col))
			.collect(Collectors.toSet());
	}

	public static Set<Seat> lockedSeats() {
		return lockedSeats("일반", "A", 1, 3);
	}

	public static Set<Seat> multipleSeats(int count) {
		return availableSeats("일반", "A", 1, count);
	}

}
