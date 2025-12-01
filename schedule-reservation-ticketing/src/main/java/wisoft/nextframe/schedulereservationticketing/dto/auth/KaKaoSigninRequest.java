package wisoft.nextframe.schedulereservationticketing.dto.auth;

import jakarta.validation.constraints.NotNull;

public record KaKaoSigninRequest(
	@NotNull
	String provider,
	@NotNull
	String authCode
) {
}
