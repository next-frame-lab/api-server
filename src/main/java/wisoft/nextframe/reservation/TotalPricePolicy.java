package wisoft.nextframe.reservation;

import java.util.Set;

import wisoft.nextframe.performance.Performance;
import wisoft.nextframe.seat.Seat;
import wisoft.nextframe.stadium.Stadium;

public class TotalPricePolicy {

	public int calculate(Performance performance, Set<Seat> seats) {
		final int basePrice = performance.getBasePrice();
		final Stadium stadium = performance.getStadium();

		return seats.stream()
			.mapToInt(seat -> basePrice + stadium.getPriceBySection(seat.getSection()))
			.sum();
	}
}
