package wisoft.nextframe.schedulereservationticketing.service.auth;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.config.jwt.JwtTokenProvider;
import wisoft.nextframe.schedulereservationticketing.dto.auth.KakaoUserInfoResponse;
import wisoft.nextframe.schedulereservationticketing.dto.auth.SigninResponse;
import wisoft.nextframe.schedulereservationticketing.entity.user.SocialAccount;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.repository.user.SocialAccountRepository;
import wisoft.nextframe.schedulereservationticketing.repository.user.UserRepository;

/**
 * OAuth 인증 후, 실제 비즈니스 로직을 처리하는 서비스 클래스
 * 조회/가입 및 내부 JWT 토큰 발급 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthSigninService {

	private final UserRepository userRepository;
	private final SocialAccountRepository socialAccountRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenService refreshTokenService;

	@Transactional
	public SigninResponse processUserSignin(String provider, KakaoUserInfoResponse userInfo) {
		// 사용자 정보로 DB에서 회원을 찾거나, 없으면 새로 가입
		final User user = saveOrUpdateUser(userInfo, provider);

		// 우리 서비스 자체 JWT(Access Token, Refresh Token)를 생성
		final String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
		final JwtTokenProvider.TokenInfo refreshTokenInfo = jwtTokenProvider.generateRefreshToken(user.getId());
		log.debug("내부 JWT 생성 완료. userId: {}", user.getId());

		// Refresh Token 저장
		refreshTokenService.saveOrUpdateRefreshToken(
			user,
			refreshTokenInfo.tokenValue(),
			refreshTokenInfo.expiresAt()
		);

		return SigninResponse.from(
			accessToken,
			refreshTokenInfo.tokenValue(),
			user.getImageUrl(),
			user.getName(),
			user.getBirthDate(),
			user.getEmail()
		);
	}

	private User saveOrUpdateUser(KakaoUserInfoResponse userInfo, String provider) {
		String providerUserId = userInfo.getId().toString();
		return socialAccountRepository.findByProviderAndProviderUserId(provider, providerUserId)
			.map(socialAccount -> {
				log.info("기존 소셜 계정으로 로그인. provider: {}, providerUserId: {}", provider, providerUserId);
				User user = socialAccount.getUser();
				user.updateProfile(userInfo.getKakaoAccount().getProfile().getNickname(), userInfo.getKakaoAccount().getProfile().getProfileImageUrl());
				return user;
			})
			.orElseGet(() -> {
				log.info("신규 소셜 계정. provider: {}, providerUserId: {}", provider, providerUserId);
				String email = userInfo.getKakaoAccount().getEmail();
				User user = userRepository.findByEmail(email)
					.orElseGet(() -> {
						log.info("신규 회원 가입. email: {}", email);
						return userRepository.save(User.builder()
							.email(email)
							.name(userInfo.getKakaoAccount().getProfile().getNickname())
							.imageUrl(userInfo.getKakaoAccount().getProfile().getProfileImageUrl())
							.birthDate(LocalDate.of(1998, 4, 7)) // 임시 생년월일
							.build());
					});

				socialAccountRepository.save(SocialAccount.builder()
					.provider(provider)
					.providerUserId(providerUserId)
					.user(user)
					.build());

				return user;
			});
	}
}
