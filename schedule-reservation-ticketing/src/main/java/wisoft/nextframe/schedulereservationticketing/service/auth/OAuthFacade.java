package wisoft.nextframe.schedulereservationticketing.service.auth;

import java.util.Map;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.dto.auth.OAuthUserInfo;
import wisoft.nextframe.schedulereservationticketing.dto.auth.SigninResponse;

/**
 * 소셜 로그인 요청의 진입점 역할을 하는 클래스입니다.
 *
 * provider에 따라 적절한 OAuthProvider를 선택하여
 * 인가 코드를 사용자 정보로 변환하고,
 * 이후 로그인/회원 처리 로직은 OAuthSigninService에 위임합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthFacade {

	private final Map<String, OAuthProvider> providerMap;
	private final OAuthSigninService signinService;

	@PostConstruct
	public void validateProviders() {
		if (providerMap == null || providerMap.isEmpty()) {
			throw new DomainException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	public SigninResponse signin(String provider, String authCode) {
		final OAuthProvider oAuthProvider = providerMap.get(provider);

		if (oAuthProvider == null) {
			throw new DomainException(ErrorCode.UNSUPPORTED_PROVIDER);
		}

		final OAuthUserInfo userInfo = oAuthProvider.getUserInfo(authCode);
		return signinService.processUserSignin(userInfo);
	}
}
