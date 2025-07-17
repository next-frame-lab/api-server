package wisoft.nextframe.user;

import java.util.UUID;

public class UserId {

	private final UUID value;

	private UserId(UUID value) {
		this.value = value;
	}

	public static UserId of(UUID value) {
		return new UserId(value);
	}

	public static UserId generate() {
		return new UserId(UUID.randomUUID());
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
