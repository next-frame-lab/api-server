package wisoft.nextframe.util;

import wisoft.nextframe.user.User;

public class UserFixture {

	private static final String DEFAULT_NAME = "사용자";
	private static final int DEFAULT_AGE = 30;
	private static final String DEFAULT_PHONE_NUMBER = "010-1111-1111";
	private static final String DEFAULT_EMAIL = "example@haha.com";
	private static final String DEFAULT_FACE_ID = "1234";

	private UserFixture() {
	}

	public static User create() {
		return create(DEFAULT_NAME, DEFAULT_AGE, DEFAULT_PHONE_NUMBER, DEFAULT_EMAIL, DEFAULT_FACE_ID);
	}

	public static User underage() {
		return create(DEFAULT_NAME, 15, DEFAULT_PHONE_NUMBER, DEFAULT_EMAIL, DEFAULT_FACE_ID);
	}

	public static User adult() {
		return create(DEFAULT_NAME, 30, DEFAULT_PHONE_NUMBER, DEFAULT_EMAIL, DEFAULT_FACE_ID);
	}

	public static User create(String name, int age, String phoneNumber, String email, String faceId) {
		return User.create(name, age, phoneNumber, email, faceId);
	}
}
