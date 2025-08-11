package wisoft.nextframe.schedulereservationticketing.schedule.domain.fixture;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import wisoft.nextframe.schedule.domain.seat.Seat;

public class SeatFixture {

	private SeatFixture() {}

	public static Seat available(String section, int row, int column) {
		return Seat.create(section, row, column);
	}

	public static Seat locked(String section, int row, int column) {
		Seat seat = Seat.create(section, row, column);
		seat.lock();
		return seat;
	}

	public static Set<Seat> empty() {
		return Set.of();
	}

	public static Set<Seat> ofCount(int count) {
		return IntStream.range(0, count)
			.mapToObj(i -> available("A", 1, i + 1))
			.collect(Collectors.toSet());
	}

	public static Set<Seat> mixedAvailableAndLocked() {
		Seat available = available("A", 1, 1);
		Seat locked = locked("A", 1, 2);
		return Set.of(available, locked);
	}

	public static Set<Seat> validInStadium() {
		return Set.of(
			Seat.create("A", 1, 1),
			Seat.create("A", 1, 2),
			Seat.create("A", 1, 3)
		);
	}

	public static Set<Seat> notInStadium() {
		return Set.of(
			Seat.create("Z", 99, 99)
		);
	}
}

