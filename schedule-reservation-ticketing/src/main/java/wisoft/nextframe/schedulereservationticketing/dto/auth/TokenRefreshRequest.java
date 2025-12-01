package wisoft.nextframe.schedulereservationticketing.dto.auth;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenRefreshRequest {
	@NotNull
	private String refreshToken;
}
