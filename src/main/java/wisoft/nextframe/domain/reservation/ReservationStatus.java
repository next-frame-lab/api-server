package wisoft.nextframe.domain.reservation;

import wisoft.nextframe.domain.reservation.exception.CannotConfirmCanceledReservationException;
import wisoft.nextframe.domain.reservation.exception.ReservationAlreadyCanceledException;
import wisoft.nextframe.domain.reservation.exception.ReservationAlreadyConfirmedException;

public enum ReservationStatus {

	CREATED {
		@Override
		public ReservationStatus confirm() {
			return CONFIRMED;
		}

		@Override
		public ReservationStatus cancel() {
			return CANCELED;
		}
	},

	CONFIRMED {
		@Override
		public ReservationStatus confirm() {
			throw new ReservationAlreadyConfirmedException();
		}

		@Override
		public ReservationStatus cancel() {
			return CANCELED;
		}
	},

	CANCELED {
		@Override
		public ReservationStatus confirm() {
			throw new CannotConfirmCanceledReservationException();
		}

		@Override
		public ReservationStatus cancel() {
			throw new ReservationAlreadyCanceledException();
		}
	};

	public abstract ReservationStatus confirm();

	public abstract ReservationStatus cancel();
}
