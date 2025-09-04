package wisoft.nextframe.schedulereservationticketing.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카카오의 Access Token으로 카카오 서버에 사용자 정보를 요청했을 때, 카카오가 보내주는 응답을 담은 DTO
 */
@Getter
@NoArgsConstructor
public class KakaoUserInfoResponse {

	private Long id;

	@JsonProperty("kakao_account")
	private KakaoAccount kakaoAccount;

	@Getter
	@NoArgsConstructor
	public static class KakaoAccount {
		private String email;
		private Profile profile;

		@Getter
		@NoArgsConstructor
		public static class Profile {
			private String nickname;
		}
	}
}
