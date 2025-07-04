package wisoft.nextframe.payment.refund;

import java.util.UUID;

public class RefundId {
	private final UUID value;

	private RefundId(UUID value) {
		this.value = value;
	}

	public static RefundId of(UUID value) {
		if (value == null) {
			throw new IllegalArgumentException("RefundId cannot be null");
		}
		return new RefundId(value);
	}

	public static RefundId generate() {
		return new RefundId(UUID.randomUUID());
	}
}
