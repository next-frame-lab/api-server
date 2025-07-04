package wisoft.nextframe.ticket;

import java.util.UUID;

public class TicketId {

	private final UUID value;

	private TicketId(UUID value) {
		this.value = value;
	}

	public static TicketId of(UUID value) {
		if (value == null) {
			throw new IllegalArgumentException("TicketId cannot be null");
		}
		return new TicketId(value);
	}

	public static TicketId generate() {
		return new TicketId(UUID.randomUUID());
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
