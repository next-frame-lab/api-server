package wisoft.nextframe.schedulereservationticketing.service.auth;

import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.dto.auth.OAuthUserInfo;
import wisoft.nextframe.schedulereservationticketing.dto.auth.SigninResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthFacade {

	private final Map<String, OAuthProvider> providerMap;
	private final OAuthSigninService signinService;

	public SigninResponse signin(String provider, String authCode) {
		final OAuthProvider oAuthProvider = providerMap.get(provider);

		if (oAuthProvider == null) {
			throw new DomainException(ErrorCode.UNSUPPORTED_PROVIDER);
		}

		final OAuthUserInfo userInfo = oAuthProvider.getUserInfo(authCode);
		return signinService.processUserSignin(userInfo);
	}
}
