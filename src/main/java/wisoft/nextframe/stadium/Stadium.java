package wisoft.nextframe.stadium;

import java.util.Map;
import java.util.Set;

import wisoft.nextframe.seat.Seat;

public class Stadium {

	private final String name;
	private final String address;
	private final Set<Seat> availableSeats;
	private final Map<String, Integer> sectionPrice;

	public Stadium(String name, String address, Set<Seat> availableSeats, Map<String, Integer> sectionPrice) {
		this.name = name;
		this.address = address;
		this.availableSeats = availableSeats;
		this.sectionPrice = sectionPrice;
	}

	public boolean hasSeat(Seat seat) {
		return availableSeats.contains(seat);
	}

	public int getPriceBySection(String section) {
		return sectionPrice.getOrDefault(section, 0);
	}
}
