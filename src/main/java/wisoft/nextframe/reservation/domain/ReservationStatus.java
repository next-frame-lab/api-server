package wisoft.nextframe.reservation.domain;

import wisoft.nextframe.reservation.domain.exception.CannotConfirmCanceledReservationException;
import wisoft.nextframe.reservation.domain.exception.ReservationAlreadyCanceledException;
import wisoft.nextframe.reservation.domain.exception.ReservationAlreadyConfirmedException;

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
