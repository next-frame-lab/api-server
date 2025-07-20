package wisoft.nextframe.util;

import java.util.Map;
import java.util.Set;

import wisoft.nextframe.common.Money;
import wisoft.nextframe.seat.Seat;
import wisoft.nextframe.stadium.Stadium;

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
		return create(DEFAULT_NAME, DEFAULT_ADDRESS, DEFAULT_AVAILABLE_SEATS, DEFAULT_SECTION_PRICE);
	}

	public static Stadium createWithSectionPrice(Map<String, Money> sectionPrice) {
		return create(DEFAULT_NAME, DEFAULT_ADDRESS, DEFAULT_AVAILABLE_SEATS, sectionPrice);
	}

	private static Stadium create(String name, String address, Set<Seat> availableSeats,
		Map<String, Money> sectionPrice) {
		return Stadium.create(name, address, availableSeats, sectionPrice);
	}

	public static Stadium createWithSeats(Set<Seat> availableSeats) {
		return create(DEFAULT_NAME, DEFAULT_ADDRESS, availableSeats, DEFAULT_SECTION_PRICE);
	}
}
