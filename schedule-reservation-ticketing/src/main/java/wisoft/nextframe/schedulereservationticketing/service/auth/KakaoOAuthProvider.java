package wisoft.nextframe.schedulereservationticketing.service.auth;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.dto.auth.KakaoUserInfoResponse;
import wisoft.nextframe.schedulereservationticketing.dto.auth.OAuthUserInfo;

@Component("kakao")
@RequiredArgsConstructor
public class KakaoOAuthProvider implements OAuthProvider{

	private final KakaoApiClient kakaoApiClient;

	@Override
	public OAuthUserInfo getUserInfo(String authCode) {
		final String accessToken = kakaoApiClient.getKakaoAccessToken(authCode);
		final KakaoUserInfoResponse kakaoUserInfo = kakaoApiClient.getKakaoUserInfo(accessToken);

		return OAuthUserInfo.fromKakao(kakaoUserInfo);
	}
}
