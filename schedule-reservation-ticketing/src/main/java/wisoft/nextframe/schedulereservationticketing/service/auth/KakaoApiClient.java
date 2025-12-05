package wisoft.nextframe.schedulereservationticketing.service.auth;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.dto.auth.KakaoTokenResponse;
import wisoft.nextframe.schedulereservationticketing.dto.auth.KakaoUserInfoResponse;

/**
 * 카카오 인증 서버 및 API 서버와 통신을 전담하는 클래스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoApiClient {

	private final OAuth2ClientProperties oAuth2ClientProperties;
	private final RestClient restClient = RestClient.create();

	/**
	 * 인가 코드를 받아 카카오 서버로부터 Access Token을 요청하고 받아옵니다.
	 * @param authCode 사용자가 리디렉션을 통해 받아온 인가 코드
	 * @return 카카오의 Access Token
	 */
	public String getKakaoAccessToken(String authCode) {
		final OAuth2ClientProperties.Registration kakaoRegistration = oAuth2ClientProperties.getRegistration().get("kakao");
		final OAuth2ClientProperties.Provider kakaoProvider = oAuth2ClientProperties.getProvider().get("kakao");

		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("grant_type", "authorization_code");
		body.add("client_id", kakaoRegistration.getClientId());
		body.add("client_secret", kakaoRegistration.getClientSecret());
		body.add("redirect_uri", kakaoRegistration.getRedirectUri());
		body.add("code", authCode);

		final KakaoTokenResponse response = restClient.post()
			.uri(kakaoProvider.getTokenUri())
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body(body)
			.retrieve()
			.onStatus(HttpStatusCode::is5xxServerError, (request, responseObj) -> {
				log.error("Kakao Server Error: {}", new String(responseObj.getBody().readAllBytes(), StandardCharsets.UTF_8));
				throw new DomainException(ErrorCode.KAKAO_SERVER_ERROR); // 502 Bad Gateway
			})
			.onStatus(HttpStatusCode::is4xxClientError, (request, responseObj) -> {
				String errorBody = new String(responseObj.getBody().readAllBytes(), StandardCharsets.UTF_8);
				log.warn("Kakao Client Error: {}", errorBody);
				throw new DomainException(ErrorCode.INVALID_KAKAO_AUTH_CODE);
			})
			.body(KakaoTokenResponse.class);

		if (response == null) {
			throw new DomainException(ErrorCode.FAILED_TO_RECEIVE_KAKAO_TOKEN);
		}
		String accessToken = response.getAccessToken();
		if (accessToken == null) {
			throw new DomainException(ErrorCode.MISSING_KAKAO_ACCESS_TOKEN);
		}
		return accessToken;
	}

	/**
	 * Access Token을 사용하여 카카오 API 서버에서 사용자 정보를 요청하고 받아옵니다.
	 * @param kakaoAccessToken 카카오 인증 서버로부터 받은 Access Token
	 * @return 카카오 사용자 정보
	 */
	public KakaoUserInfoResponse getKakaoUserInfo(String kakaoAccessToken) {
		final OAuth2ClientProperties.Provider kakaoProvider = oAuth2ClientProperties.getProvider().get("kakao");

		final KakaoUserInfoResponse response = restClient.get()
			.uri(kakaoProvider.getUserInfoUri())
			.header("Authorization", "Bearer " + kakaoAccessToken)
			.retrieve()
			.body(KakaoUserInfoResponse.class);

		return Optional.ofNullable(response)
			.orElseThrow(() -> new DomainException(ErrorCode.FAILED_TO_GET_KAKAO_USER_INFO));
	}
}
