package wisoft.nextframe.reservation;

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
			throw new IllegalStateException("이미 확정된 예매입니다.");
		}

		@Override
		public ReservationStatus cancel() {
			return CANCELED;
		}
	},

	CANCELED {
		@Override
		public ReservationStatus confirm() {
			throw new IllegalStateException("취소된 예매는 확정할 수 없습니다.");
		}

		@Override
		public ReservationStatus cancel() {
			throw new IllegalStateException("이미 취소된 예매입니다.");
		}
	};

	public abstract ReservationStatus confirm();

	public abstract ReservationStatus cancel();

	public ReservationStatus transitionTo(TransitionType transition) {
		return switch (transition) {
			case CONFIRM -> confirm();
			case CANCEL -> cancel();
		};
	}
}
