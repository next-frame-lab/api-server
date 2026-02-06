package wisoft.nextframe.payment.application.payment.port.output;

import wisoft.nextframe.payment.domain.ReservationId;

public interface ReservationCancelClient {
	/**
	 * 예약을 취소합니다.
	 * 이미 취소된 예약에 대해서는 멱등하게 200 OK를 반환합니다.
	 *
	 * @param reservationId 취소할 예약 ID
	 */
	void cancelReservation(ReservationId reservationId);
}