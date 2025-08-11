package wisoft.nextframe.schedule;

import java.util.Map;
import java.util.Set;

import wisoft.nextframe.common.Money;
import wisoft.nextframe.schedule.domain.seat.Seat;
import wisoft.nextframe.schedule.domain.stadium.Stadium;

public class StadiumFixture {

	private static final String DEFAULT_NAME = "기본 공연장";
	private static final String DEFAULT_ADDRESS = "대전 광역시";
	private static final Set<Seat> DEFAULT_AVAILABLE_SEATS = Set.of(
		SeatFixture.available("A", 1, 1),
		SeatFixture.available("A", 1, 2),
		SeatFixture.available("B", 2, 1),
		SeatFixture.available("B", 2, 2)
	);
	private static final Map<String, Money> DEFAULT_SECTION_PRICE = Map.of(
		"A", Money.of(20_000),
		"B", Money.of(0)
	);

	private StadiumFixture() {
	}

	public static Stadium defaultStadium() {
		return create(DEFAULT_NAME, DEFAULT_ADDRESS, DEFAULT_AVAILABLE_SEATS);
	}

	private static Stadium create(String name, String address, Set<Seat> availableSeats) {
		return Stadium.create(name, address, availableSeats);
	}

	public static Stadium createWithSeats(Set<Seat> availableSeats) {
		return create(DEFAULT_NAME, DEFAULT_ADDRESS, availableSeats);
	}
}
