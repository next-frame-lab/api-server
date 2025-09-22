package wisoft.nextframe.schedulereservationticketing.service.auth;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.config.jwt.JwtTokenProvider;
import wisoft.nextframe.schedulereservationticketing.dto.auth.KakaoUserInfoResponse;
import wisoft.nextframe.schedulereservationticketing.dto.auth.SigninResponse;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.repository.user.UserRepository;

@Service
@RequiredArgsConstructor
public class OAuthService {

	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final AuthService authService;
	private final KakaoApiClient kakaoApiClient;

	/**
	 * 인가 코드를 받아 카카오 로그인을 처리하는 메서드
	 */
	@Transactional
	public SigninResponse kakaoSignin(String provider, String authCode) {
		// 1. 외부 API 연동은 KakaoApiClient에 위임
		final String kakaoAccessToken = kakaoApiClient.getKakaoAccessToken(authCode);
		final KakaoUserInfoResponse userInfo = kakaoApiClient.getKakaoUserInfo(kakaoAccessToken);

		// 2. 사용자 정보로 DB에서 회원을 찾거나, 없으면 새로 가입 (핵심 비즈니스 로직)
		final User user = saveOrUpdateUser(userInfo, provider);

		// 3. 우리 서비스 자체 JWT(Access Token, Refresh Token)를 생성
		final String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
		final String refreshTokenValue = jwtTokenProvider.generateRefreshToken(user.getId());

		// 4. Refresh Token 저장 로직은 AuthService에 위임
		final LocalDateTime refreshTokenExpiresAt = jwtTokenProvider.getExpirationDateFromToken(refreshTokenValue);
		authService.saveOrUpdateRefreshToken(user, refreshTokenValue, refreshTokenExpiresAt);

		return SigninResponse.from(
			accessToken,
			refreshTokenValue,
			user.getImageUrl(),
			user.getName(),
			user.getBirthDate(),
			user.getEmail()
		);
	}

	private User saveOrUpdateUser(KakaoUserInfoResponse userInfo, String provider) {
		final String email = userInfo.getKakaoAccount().getEmail();
		final String nickname = userInfo.getKakaoAccount().getProfile().getNickname();
		final String imageUrl = userInfo.getKakaoAccount().getProfile().getProfileImageUrl();

		return userRepository.findByEmailAndProvider(email, provider)
			.map(user -> user.updateProfile(nickname, imageUrl)) // 이미 존재하면 프로필 업데이트
			.orElseGet(() -> userRepository.save(User.builder() // 없으면 새로 생성하여 저장
				.email(email)
				.name(nickname)
				.imageUrl(imageUrl)
				.provider(provider)
				.birthDate(LocalDate.of(1998, 4, 7))
				.build()));
	}
}
