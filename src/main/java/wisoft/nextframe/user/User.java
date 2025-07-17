package wisoft.nextframe.user;

import java.time.LocalDate;
import java.time.Period;

public class User {

	private final UserId id;
	private final String name;
	private final LocalDate birthDate;
	private final String phoneNumber;
	private final String email;
	private final String faceId;

	private User(UserId id, String name, LocalDate birthDate, String phoneNumber, String email, String faceId) {
		this.id = id;
		this.name = name;
		this.birthDate = birthDate;
		this.phoneNumber = phoneNumber;
		this.email = email;
		this.faceId = faceId;
	}

	public boolean isAdult() {
		return Period.between(birthDate, LocalDate.now()).getYears() >= 19;
	}

	public String getName() {
		return name;
	}

	public static User create(String name, LocalDate birthDate, String phoneNumber, String email, String faceId) {
		return new User(UserId.generate(), name, birthDate, phoneNumber, email, faceId);
	}
}
