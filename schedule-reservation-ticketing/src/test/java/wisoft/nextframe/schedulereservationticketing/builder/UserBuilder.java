package wisoft.nextframe.schedulereservationticketing.builder;

import java.time.LocalDate;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserBuilder {

	private UUID id = null;
	private String name = "테스트 사용자";
	private String email = "test@example.com";
	private LocalDate birthDate = LocalDate.of(1990, 1, 1);
	private String phoneNumber = "010-1234-5678";
	private String imageUrl = "https://example.com/test_image.jpg";

	public static UserBuilder builder() {
		return new UserBuilder();
	}

	public UserBuilder withName(String name) {
		this.name = name;
		return this;
	}

	public UserBuilder withBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
		return this;
	}

	public User build() {
		return new User(id, name, email, birthDate, phoneNumber, imageUrl);
	}
}
