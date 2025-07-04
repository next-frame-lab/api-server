package wisoft.nextframe.user;

public class User {

	private final String name;
	private final int age;
	private final String phoneNumber;
	private final String email;
	private final String faceId;

	private User(String name, int age, String phoneNumber, String email, String faceId) {
		this.name = name;
		this.age = age;
		this.phoneNumber = phoneNumber;
		this.email = email;
		this.faceId = faceId;
	}

	public boolean isAdult() {
		return age >= 19;
	}

	public String getName() {
		return name;
	}

	public static User create(String name, int age, String phoneNumber, String email, String faceId) {
		return new User(name, age, phoneNumber, email, faceId);
	}
}
