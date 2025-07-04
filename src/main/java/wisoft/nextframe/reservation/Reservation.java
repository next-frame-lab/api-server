package wisoft.nextframe.reservation;

import java.util.Set;

import lombok.Getter;
import wisoft.nextframe.performance.Performance;
import wisoft.nextframe.seat.Seat;
import wisoft.nextframe.stadium.Stadium;
import wisoft.nextframe.user.User;

@Getter
public class Reservation {

	private final Performance performance;
	private final User user;
	private final Set<Seat> reservedSeats;
	private ReservationStatus status;

	private Reservation(Performance performance, User user, Set<Seat> reservedSeats, ReservationStatus status) {
		this.performance = performance;
		this.user = user;
		this.reservedSeats = reservedSeats;
		this.status = status;
	}

	public static Reservation create(User user, Performance performance, Set<Seat> selectedSeats, Long elapsedTime) {
		ReservationPolicy.validateBeforeReservation(user, performance, selectedSeats, elapsedTime);

		selectedSeats.forEach(Seat::lock);

		return new Reservation(
			performance,
			user,
			selectedSeats,
			ReservationStatus.CREATED
		);
	}

	public void changeStatusTo(TransitionType transition) {
		this.status = this.status.transitionTo(transition);

		if (transition == TransitionType.CANCEL) {
			this.reservedSeats.forEach(Seat::unlock);
		}
	}

	public int calculateReservationPrice() {
		final int basePrice = performance.getBasePrice();
		Stadium stadium = performance.getStadium();

		return reservedSeats.stream()
			.mapToInt(seat -> basePrice + stadium.getPriceBySection(seat.getSection()))
			.sum();
	}
}
