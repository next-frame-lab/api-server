package wisoft.nextframe.payment.domain.payment.exception;

public class MissingReservationException extends PaymentException {
	public MissingReservationException() {
		super("결제를 위해서는 예매 정보가 필요합니다.");
	}
}