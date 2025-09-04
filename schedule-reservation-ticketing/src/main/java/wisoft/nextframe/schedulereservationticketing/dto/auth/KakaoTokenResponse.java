package wisoft.nextframe.schedulereservationticketing.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 인가 코드로 카카오 서버에 토큰을 요청했을 때, 카카오가 보내주는 응답(Access Token, Refresh Token 등)을 담을 DTO
 */
@Getter
@NoArgsConstructor
public class KakaoTokenResponse {

	@JsonProperty("token_type")
	private String tokenType;

	@JsonProperty("access_token")
	private String accessToken;

	@JsonProperty("expires_in")
	private Integer expiresIn;

	@JsonProperty("refresh_token")
	private String refreshToken;

	@JsonProperty("refresh_token_expires_in")
	private Integer refreshTokenExpiresIn;
}
