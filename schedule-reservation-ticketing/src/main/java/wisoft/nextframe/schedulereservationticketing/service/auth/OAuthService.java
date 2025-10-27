package wisoft.nextframe.schedulereservationticketing.service.auth;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.dto.auth.KakaoUserInfoResponse;
import wisoft.nextframe.schedulereservationticketing.dto.auth.SigninResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {

	private final KakaoApiClient kakaoApiClient;
	private final OAuthSigninService oAuthSiginService;

	public SigninResponse kakaoSignin(String provider, String authCode) {
		log.debug("카카오 Access Token 요청 시작.");
		final String kakaoAccessToken = kakaoApiClient.getKakaoAccessToken(authCode);
		log.debug("카카오 사용자 정보 요청 시작.");
		final KakaoUserInfoResponse userInfo = kakaoApiClient.getKakaoUserInfo(kakaoAccessToken);
		log.debug("카카오 사용자 정보 수신 완료. email: {}", userInfo.getKakaoAccount().getEmail());

		return oAuthSiginService.processUserSignin(provider, userInfo);
	}
}
