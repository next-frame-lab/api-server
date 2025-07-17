package wisoft.nextframe.reservation;

import java.util.Set;

import lombok.Getter;
import wisoft.nextframe.performance.Performance;
import wisoft.nextframe.seat.Seat;
import wisoft.nextframe.user.User;

@Getter
public class Reservation {

	private final ReservationId id;
	private final Performance performance;
	private final User user;
	private final Set<Seat> reservedSeats;
	private final int totalPrice;
	private ReservationStatus status;

	private Reservation(
		ReservationId id,
		Performance performance,
		User user,
		Set<Seat> reservedSeats,
		int totalPrice,
		ReservationStatus status
	) {
		this.id = id;
		this.performance = performance;
		this.user = user;
		this.reservedSeats = reservedSeats;
		this.totalPrice = totalPrice;
		this.status = status;
	}

	public static Reservation create(User user, Performance performance, Set<Seat> selectedSeats, int totalPrice) {
		return new Reservation(
			ReservationId.generate(),
			performance,
			user,
			selectedSeats,
			totalPrice,
			ReservationStatus.CREATED);
	}

	public void cancel() {
		this.status = this.status.cancel();
	}

	public void confirm() {
		this.status = this.status.confirm();
	}
}
