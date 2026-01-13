package wisoft.nextframe.schedulereservationticketing.service.auth;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.config.jwt.JwtTokenProvider;
import wisoft.nextframe.schedulereservationticketing.dto.auth.OAuthUserInfo;
import wisoft.nextframe.schedulereservationticketing.dto.auth.SigninResponse;
import wisoft.nextframe.schedulereservationticketing.entity.user.SocialAccount;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.repository.user.SocialAccountRepository;
import wisoft.nextframe.schedulereservationticketing.repository.user.UserRepository;

/**
 * OAuth 인증이 완료된 사용자 정보를 기반으로
 * 실제 로그인/회원 처리와 토큰 발급을 담당하는 서비스입니다.
 *
 * - 소셜 계정 및 사용자 조회/생성
 * - 내부 JWT(Access / Refresh Token) 발급
 * - Refresh Token 저장 및 교체
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
	public SigninResponse processUserSignin(OAuthUserInfo userInfo) {
		// 사용자 정보로 DB에서 회원을 찾거나, 없으면 새로 가입
		final User user = findOrCreateUser(userInfo);

		// 서비스 자체 토큰(Refresh, Access Token) 발급
		final String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
		final JwtTokenProvider.TokenInfo refreshTokenInfo = jwtTokenProvider.generateRefreshToken(user.getId());
		log.debug("내부 JWT 생성 완료. userId: {}", user.getId());

		// Refresh Token 저장
		refreshTokenService.replaceRefreshToken(
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

	private User findOrCreateUser(OAuthUserInfo userInfo) {
		String provider = userInfo.provider();
		String providerUserId = userInfo.providerUserId();

		// 1. 소셜 계정이 이미 존재하는 경우
		Optional<SocialAccount> socialAccountOptional =
			socialAccountRepository.findByProviderAndProviderUserId(
				provider,
				providerUserId
			);
		if (socialAccountOptional.isPresent()) {
			log.info("기존 소셜 계정 로그인. provider={}, providerUserId={}", provider, providerUserId);
			User user = socialAccountOptional.get().getUser();
			user.updateProfile(userInfo.name(), userInfo.imageUrl());
			return user;
		}

		// 2. 소셜 계정은 없지만, 동일 이메일의 User가 존재하는 경우
		User user = userRepository.findByEmail(userInfo.email())
			.orElseGet(() -> {
				log.info("신규 회원 가입. email={}", userInfo.email());
				return userRepository.save(
					User.builder()
						.email(userInfo.email())
						.name(userInfo.name())
						.imageUrl(userInfo.imageUrl())
						.birthDate(null) // 소셜 제공자에 따라 추후 보완
						.build()
				);
			});

		// 3. 새로운 SocialAccount 생성 및 연동
		log.info("소셜 계정 연동. provider={}, providerUserId={}, userId={}", provider, providerUserId, user.getId());
		socialAccountRepository.save(
			SocialAccount.builder()
				.provider(provider)
				.providerUserId(providerUserId)
				.user(user)
				.build()
		);

		return user;
	}
}
