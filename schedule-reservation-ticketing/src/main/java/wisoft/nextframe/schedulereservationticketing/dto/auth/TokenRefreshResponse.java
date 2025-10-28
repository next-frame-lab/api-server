package wisoft.nextframe.schedulereservationticketing.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor // 생성자를 통해 accessToken 값을 받도록 설정
public class TokenRefreshResponse {
	private String accessToken;
}
