package wisoft.nextframe.domain.stadium;

import java.util.Map;
import java.util.Set;

import wisoft.nextframe.common.Money;
import wisoft.nextframe.domain.seat.Seat;

public class Stadium {

	private final StadiumId id;
	private final String name;
	private final String address;
	private final Set<Seat> availableSeats;
	private final Map<String, Money> sectionPrice;

	private Stadium(StadiumId id, String name, String address, Set<Seat> availableSeats, Map<String, Money> sectionPrice) {
		this.id = id;
		this.name = name;
		this.address = address;
		this.availableSeats = availableSeats;
		this.sectionPrice = sectionPrice;
	}

	public boolean hasSeat(Seat seat) {
		return availableSeats.contains(seat);
	}

	public Money getPriceBySection(String section) {
		return sectionPrice.getOrDefault(section, Money.ZERO);
	}

	public static Stadium create(String name, String address, Set<Seat> availableSeats,
		Map<String, Money> sectionPrice) {
		return new Stadium(StadiumId.generate(), name, address, availableSeats, sectionPrice);
	}
}
