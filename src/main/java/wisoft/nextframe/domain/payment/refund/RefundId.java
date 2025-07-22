package wisoft.nextframe.domain.payment.refund;

import java.util.Objects;
import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class RefundId {
	private final UUID value;

	private RefundId(UUID value) {
		this.value = Objects.requireNonNull(value, "RefundId는 null일 수 없습니다.");
	}

	public static RefundId of(UUID value) {
		return new RefundId(value);
	}

	public static RefundId generate() {
		return new RefundId(UUID.randomUUID());
	}
}
