package wisoft.nextframe.schedulereservationticketing.reservation.domain;

import java.util.Set;

import wisoft.nextframe.schedulereservationticketing.schedule.domain.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.reservation.domain.validator.ReservationValidator;
import wisoft.nextframe.schedulereservationticketing.schedule.domain.seat.Seat;
import wisoft.nextframe.schedulereservationticketing.schedule.domain.seat.SeatManager;
import wisoft.nextframe.schedulereservationticketing.user.domain.User;

public class ReservationService {

	private final ReservationValidator validator;
	private final SeatManager seatManager;
	private final ReservationFactory reservationFactory;

	public ReservationService(ReservationValidator validator, SeatManager seatManager,
		ReservationFactory reservationFactory) {
		this.validator = validator;
		this.seatManager = seatManager;
		this.reservationFactory = reservationFactory;
	}

	public Reservation createReservation(
		User user,
		Performance performance,
		Set<Seat> selectedSeats,
		ElapsedTime elapsedTime
	) {
		validator.validate(user, performance, selectedSeats, elapsedTime); // 검증
		seatManager.lockSeats(selectedSeats); // 좌석 잠금
		return reservationFactory.create(user, performance, selectedSeats);
	}

	public void cancelReservation(Reservation reservation) {
		reservation.cancel();
		seatManager.unlockSeats(reservation.getReservedSeats());
	}
}
