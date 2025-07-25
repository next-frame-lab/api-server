package wisoft.nextframe.domain.stadium;

import java.util.Set;

import wisoft.nextframe.domain.seat.Seat;

public class Stadium {

	private final StadiumId id;
	private final String name;
	private final String address;
	private final Set<Seat> availableSeats;

	private Stadium(StadiumId id, String name, String address, Set<Seat> availableSeats) {
		this.id = id;
		this.name = name;
		this.address = address;
		this.availableSeats = availableSeats;
	}

	public boolean hasSeat(Seat seat) {
		return availableSeats.contains(seat);
	}

	public static Stadium create(String name, String address, Set<Seat> availableSeats) {
		return new Stadium(StadiumId.generate(), name, address, availableSeats);
	}
}
