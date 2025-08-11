package wisoft.nextframe.payment.domain.exception;

import java.util.Arrays;
import java.util.stream.Collectors;

import wisoft.nextframe.payment.domain.PaymentStatus;

public class InvalidPaymentStatusException extends PaymentException {
	public InvalidPaymentStatusException(String action, PaymentStatus actualStatus, PaymentStatus... expectedStatuses) {
		super(String.format(
			"%s 처리는 현재 상태(%s)에서는 불가능합니다. 허용 상태: %s",
			action,
			actualStatus.name(),
			Arrays.stream(expectedStatuses)
				.map(Enum::name)
				.collect(Collectors.joining(", "))
		));
	}
}