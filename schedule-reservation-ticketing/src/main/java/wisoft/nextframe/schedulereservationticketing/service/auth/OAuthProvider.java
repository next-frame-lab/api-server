package wisoft.nextframe.schedulereservationticketing.service.auth;

import wisoft.nextframe.schedulereservationticketing.dto.auth.OAuthUserInfo;

public interface OAuthProvider {

	OAuthUserInfo getUserInfo(String authCode);
}
