package wisoft.nextframe.schedulereservationticketing.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenRefreshResponse {
	private String accessToken;
}
