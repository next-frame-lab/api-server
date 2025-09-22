package wisoft.nextframe.schedulereservationticketing.service.auth;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.config.jwt.JwtTokenProvider;
import wisoft.nextframe.schedulereservationticketing.dto.auth.KakaoTokenResponse;
import wisoft.nextframe.schedulereservationticketing.dto.auth.KakaoUserInfoResponse;
import wisoft.nextframe.schedulereservationticketing.dto.auth.SigninResponse;
import wisoft.nextframe.schedulereservationticketing.entity.user.RefreshToken;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.repository.user.RefreshTokenRepository;
import wisoft.nextframe.schedulereservationticketing.repository.user.UserRepository;

@Service
@RequiredArgsConstructor
public class OAuthService {

	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final OAuth2ClientProperties oAuth2ClientProperties;

	/**
	 * 인가 코드를 받아 카카오 로그인을 처리하는 메서드
	 */
	@Transactional
	public SigninResponse kakaoSignin(String provider, String authCode) {
		// 1. 인가 코드로 카카오로부터 Access Token을 발급받습니다.
		final String kakaoAccessToken = getKakaoAccessToken(authCode);

		// 2. 발급 받은 Access Token으로 카카오 사용자 정보를 가져옵니다.
		final KakaoUserInfoResponse userInfo = getKakaoUserInfo(kakaoAccessToken);

		// 3. 가져온 사용자 정보로 DB에서 회원을 찾거나, 없으면 새로 가입시킵니다.
		final User user = saveOrUpdateUser(userInfo, provider);

		// 4. 우리 서비스 자체 JWT(Access Token, Refresh Token)를 생성합니다.
		final String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
		final String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

		// 5. Refresh Token을 DB에 저장 또는 업데이트합니다.
		final LocalDateTime refreshTokenExpiresAt = jwtTokenProvider.getExpirationDateFromToken(refreshToken);
		saveOrUpdateRefreshToken(user, refreshToken, refreshTokenExpiresAt);

		return SigninResponse.from(
			accessToken,
			refreshToken,
			user.getImageUrl(),
			user.getName(),
			user.getBirthDate(),
			user.getEmail()
		);
	}

	/**
	 * 1단계: 카카오 인증 서버에 인가 코드를 보내 Access Token을 받아오는 메서드
	 */
	private String getKakaoAccessToken(String authCode) {
		// 2. application.yml의 설정 정보를 객체로부터 가져옵니다.
		final OAuth2ClientProperties.Registration kakaoRegistration = oAuth2ClientProperties.getRegistration().get("kakao");
		final OAuth2ClientProperties.Provider kakaoProvider = oAuth2ClientProperties.getProvider().get("kakao");

		final RestClient restClient = RestClient.create();

		final KakaoTokenResponse response = restClient.post()
			.uri(kakaoProvider.getTokenUri())
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body("grant_type=authorization_code"
				+ "&client_id=" + kakaoRegistration.getClientId()
				+ "&client_secret=" + kakaoRegistration.getClientSecret()
				+ "&redirect_uri=" + kakaoRegistration.getRedirectUri()
				+ "&code=" + authCode)
			.retrieve() // 요청을 보내고 응답을 받습니다.
			.body(KakaoTokenResponse.class);

		// Optional.ofNullable을 사용하여 null 체크를 강화할 수 있습니다.
		return Optional.ofNullable(response)
			.map(KakaoTokenResponse::getAccessToken)
			.orElseThrow(() -> new RuntimeException("카카오 Access Token을 받아오는데 실패했습니다."));
	}

	/**
	 * 2단계: Access Token을 사용해 카카오 API 서버에서 사용자 정보를 받아오는 메서드
	 */
	private KakaoUserInfoResponse getKakaoUserInfo(String kakaoAccessToken) {
		final OAuth2ClientProperties.Provider kakaoProvider = oAuth2ClientProperties.getProvider().get("kakao");
		final RestClient restClient = RestClient.create();

		return restClient.get()
			.uri(kakaoProvider.getUserInfoUri())
			.header("Authorization", "Bearer " + kakaoAccessToken)
			.retrieve()
			.body(KakaoUserInfoResponse.class);
	}

	/**
	 * 3단계: 사용자 정보를 바탕으로 회원을 저장하거나 업데이터하는 메서드
	 */
	private User saveOrUpdateUser(KakaoUserInfoResponse userInfo, String provider) {
		final String email = userInfo.getKakaoAccount().getEmail();
		final String nickname = userInfo.getKakaoAccount().getProfile().getNickname();
		final String imageUrl = userInfo.getKakaoAccount().getProfile().getProfileImageUrl();

		final Optional<User> userOptional = userRepository.findByEmailAndProvider(email, provider);

		User user;
		if (userOptional.isPresent()) {
			// 이미 가입된 회원이면, 최신 정보(이름, 프로필 이미지)로 업데이트합니다.
			user = userOptional.get();
			user.updateProfile(nickname, imageUrl);
		} else {
			user = User.builder()
				.email(email)
				.name(nickname)
				.imageUrl(imageUrl)
				.provider(provider)
				.birthDate(LocalDate.of(1998,4,7))
				.build();
			userRepository.save(user);
		}

		return user;
	}

	private void saveOrUpdateRefreshToken(User user, String tokenValue, LocalDateTime expiresAt) {
		Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByUser(user);

		if (optionalRefreshToken.isPresent()) {
			// 기존 토큰이 있으면, 값과 만료 시간을 모두 업데이트
			RefreshToken refreshToken = optionalRefreshToken.get();
			refreshToken.updateTokenValue(tokenValue, expiresAt);
		} else {
			// 기존 토큰이 없으면, 새로 생성하여 저장
			RefreshToken newRefreshToken = RefreshToken.builder()
				.user(user)
				.tokenValue(tokenValue)
				.expiresAt(expiresAt)
				.build();
			refreshTokenRepository.save(newRefreshToken);
		}
	}
}
