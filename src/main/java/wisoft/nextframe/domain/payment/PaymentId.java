package wisoft.nextframe.domain.payment;

import java.util.Objects;
import java.util.UUID;

import lombok.Getter;

@Getter
public class PaymentId {
	private final UUID value;

	private PaymentId(UUID value) {
		this.value = Objects.requireNonNull(value, "PaymentId는 null일 수 없습니다.");
	}

	public static PaymentId of(UUID value) {
		return new PaymentId(value);
	}

	public static PaymentId of() {
		return new PaymentId(UUID.randomUUID());
	}
}
