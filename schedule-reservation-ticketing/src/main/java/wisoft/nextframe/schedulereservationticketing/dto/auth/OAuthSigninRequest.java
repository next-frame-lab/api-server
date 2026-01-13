package wisoft.nextframe.schedulereservationticketing.dto.auth;

import jakarta.validation.constraints.NotNull;

public record OAuthSigninRequest(
	@NotNull
	String provider,
	@NotNull
	String authCode
) {
}
