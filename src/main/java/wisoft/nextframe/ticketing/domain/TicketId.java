package wisoft.nextframe.ticketing.domain;

import java.util.Objects;
import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class TicketId {

	private final UUID value;

	private TicketId(UUID value) {
		this.value = Objects.requireNonNull(value, "TicketId는 null일 수 없습니다.");
	}

	public static TicketId of(UUID value) {
		return new TicketId(value);
	}

	public static TicketId generate() {
		return new TicketId(UUID.randomUUID());
	}
}
