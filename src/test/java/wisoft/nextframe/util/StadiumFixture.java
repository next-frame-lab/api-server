package wisoft.nextframe.util;

import java.util.Map;
import java.util.Set;

import wisoft.nextframe.seat.Seat;
import wisoft.nextframe.stadium.Stadium;

public class StadiumFixture {

	private static final String DEFAULT_NAME = "기본 공연장";
	private static final String DEFAULT_ADDRESS = "대전 광역시";
	private static final Set<Seat> DEFAULT_AVAILABLE_SEATS = Set.of(
		SeatFixture.available("VIP", "A", 1),
		SeatFixture.available("VIP", "A", 2),
		SeatFixture.available("일반", "B", 1),
		SeatFixture.available("일반", "B", 2)
	);
	private static final Map<String, Integer> DEFAULT_SECTION_PRICE = Map.of(
		"VIP", 20_000,
		"일반", 0
	);

	private StadiumFixture() {
	}

	public static Stadium defaultStadium() {
		return create(DEFAULT_NAME, DEFAULT_ADDRESS, DEFAULT_AVAILABLE_SEATS, DEFAULT_SECTION_PRICE);
	}

	public static Stadium createWith(Map<String, Integer> sectionPrice) {
		return create(DEFAULT_NAME, DEFAULT_ADDRESS, DEFAULT_AVAILABLE_SEATS, sectionPrice);
	}

	private static Stadium create(String name, String address, Set<Seat> availableSeats,
		Map<String, Integer> sectionPrice) {
		return new Stadium(name, address, availableSeats, sectionPrice);
	}
}
