package wisoft.nextframe.schedulereservationticketing.dto.auth;

import java.time.LocalDate;
import java.time.Period;

public record SigninResponse(
	String accessToken,
	String refreshToken,
	String imageUrl,
	String name,
	int age,
	String email
) {

	public static SigninResponse from(
		String accessToken,
		String refreshToken,
		String imageUrl,
		String name,
		LocalDate birthDate,
		String email
	) {

		final Period period = Period.between(birthDate, LocalDate.now());
		final int age = period.getYears();

		return new SigninResponse(accessToken, refreshToken, imageUrl, name, age, email);
	}
}
