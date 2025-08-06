package wisoft.nextframe.user.domain;

import java.time.LocalDate;
import java.time.Period;

import lombok.Getter;

@Getter
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

	public static User create(String name, LocalDate birthDate, String phoneNumber, String email, String faceId) {
		return new User(UserId.generate(), name, birthDate, phoneNumber, email, faceId);
	}

	/**
	 * [도메인 객체 복원용 팩토리 메서드]
	 * <p>
	 * 이 메서드는 DB 조회, JPA 매핑, 외부 시스템(JSON, Kafka 등)으로부터 전달받은 데이터를 바탕으로
	 * 도메인 객체를 복원할 때만 사용해야 합니다.
	 * <p>
	 * 도메인 내부의 비즈니스 로직에서는 절대 이 메서드를 직접 사용하지 마세요.
	 * 새로운 User를 생성하려면 {@link #create(String, LocalDate, String, String, String)}를 사용하세요.
	 */
	public static User reconstruct(
		UserId id,
		String name,
		LocalDate birthDate,
		String phoneNumber,
		String email,
		String faceId
	) {
		return new User(id, name, birthDate, phoneNumber, email, faceId);
	}
}
