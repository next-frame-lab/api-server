package wisoft.nextframe.reservation;

import java.util.Set;

import wisoft.nextframe.performance.Performance;
import wisoft.nextframe.seat.Seat;
import wisoft.nextframe.seat.SeatManager;
import wisoft.nextframe.user.User;
import wisoft.nextframe.validator.ReservationValidator;

public class ReservationService {

	private final ReservationValidator validator;
	private final SeatManager seatManager;
	private final TotalPricePolicy pricePolicy;

	public ReservationService(ReservationValidator validator, SeatManager seatManager, TotalPricePolicy pricePolicy) {
		this.validator = validator;
		this.seatManager = seatManager;
		this.pricePolicy = pricePolicy;
	}

	public Reservation createReservation(User user, Performance performance, Set<Seat> selectedSeats, Long elapsedTime) {
		validator.validate(user, performance, selectedSeats, elapsedTime); // 검증
		seatManager.lockSeats(selectedSeats); // 좌석 잠금
		final int totalPrice = pricePolicy.calculate(performance, selectedSeats); // 결제 금액 계산

		return Reservation.create(user, performance, selectedSeats, totalPrice);
	}

	public void cancelReservation(Reservation reservation) {
		reservation.cancel();
		seatManager.unlockSeats(reservation.getReservedSeats());
	}
}
