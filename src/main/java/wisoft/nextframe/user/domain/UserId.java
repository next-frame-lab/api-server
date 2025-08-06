package wisoft.nextframe.user.domain;

import java.util.Objects;
import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class UserId {

	private final UUID value;

	private UserId(UUID value) {
		this.value = Objects.requireNonNull(value, "UserId는 null일 수 없습니다.");
	}

	public static UserId of(UUID value) {
		return new UserId(value);
	}

	public static UserId generate() {
		return new UserId(UUID.randomUUID());
	}
}
