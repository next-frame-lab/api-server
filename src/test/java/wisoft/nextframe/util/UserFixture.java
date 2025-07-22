package wisoft.nextframe.util;

import java.time.LocalDate;

import wisoft.nextframe.domain.user.User;

public class UserFixture {

	private static final String DEFAULT_NAME = "사용자";
	private static final LocalDate DEFAULT_BIRTH_DATE = LocalDate.now().minusYears(25);
	private static final String DEFAULT_PHONE_NUMBER = "010-1111-1111";
	private static final String DEFAULT_EMAIL = "example@haha.com";
	private static final String DEFAULT_FACE_ID = "1234";

	private UserFixture() {
	}

	public static User defaultUser() {
		return create(DEFAULT_NAME, DEFAULT_BIRTH_DATE, DEFAULT_PHONE_NUMBER, DEFAULT_EMAIL, DEFAULT_FACE_ID);
	}

	public static User underage() {
		return create(DEFAULT_NAME, LocalDate.now().minusYears(10), DEFAULT_PHONE_NUMBER, DEFAULT_EMAIL, DEFAULT_FACE_ID);
	}

	public static User adult() {
		return create(DEFAULT_NAME, DEFAULT_BIRTH_DATE, DEFAULT_PHONE_NUMBER, DEFAULT_EMAIL, DEFAULT_FACE_ID);
	}

	public static User create(String name, LocalDate birthDate, String phoneNumber, String email, String faceId) {
		return User.create(name, birthDate, phoneNumber, email, faceId);
	}
}
