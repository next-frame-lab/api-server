package wisoft.nextframe.schedulereservationticketing.dto.auth;

public record KaKaoSigninRequest(
	String provider,
	String authCode
) {
}
