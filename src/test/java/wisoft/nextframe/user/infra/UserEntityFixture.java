package wisoft.nextframe.util;

import java.time.LocalDate;
import java.util.UUID;

import wisoft.nextframe.user.domain.User;
import wisoft.nextframe.user.domain.UserId;
import wisoft.nextframe.user.infra.UserEntity;

// UserEntityFixture.java
public class UserEntityFixture {
	public static UserEntity sampleEntity() {
		return UserEntity.builder()
			.id(UUID.fromString("f1000000-aaaa-bbbb-cccc-000000000001"))
			.name("홍길동")
			.birthDate(LocalDate.of(1990, 1, 1))
			.phoneNumber("010-2222-3333")
			.email("hong@example.com")
			.faceId("face-999")
			.build();
	}

	public static User sampleDomain() {
		return User.reconstruct(
			UserId.of(UUID.fromString("f1000000-aaaa-bbbb-cccc-000000000001")),
			"홍길동",
			LocalDate.of(1990, 1, 1),
			"010-2222-3333",
			"hong@example.com",
			"face-999"
		);
	}
}
