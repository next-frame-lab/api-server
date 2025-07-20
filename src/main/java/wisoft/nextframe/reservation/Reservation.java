package wisoft.nextframe.reservation;

import java.util.Set;

import lombok.Getter;
import wisoft.nextframe.common.Money;
import wisoft.nextframe.performance.Performance;
import wisoft.nextframe.seat.Seat;
import wisoft.nextframe.user.User;

public class Reservation {

	private final ReservationId id;
	private final Performance performance;
	private final User user;
	@Getter
	private final Set<Seat> reservedSeats;
	private final Money totalPrice;
	@Getter
	private ReservationStatus status;

	private Reservation(
		ReservationId id,
		Performance performance,
		User user,
		Set<Seat> reservedSeats,
		Money totalPrice,
		ReservationStatus status
	) {
		this.id = id;
		this.performance = performance;
		this.user = user;
		this.reservedSeats = reservedSeats;
		this.totalPrice = totalPrice;
		this.status = status;
	}

	public static Reservation create(User user, Performance performance, Set<Seat> selectedSeats, Money totalPrice) {
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
