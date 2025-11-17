package wisoft.nextframe.schedulereservationticketing.dto.auth;

public record OAuthUserInfo(
	String provider,
	String providerUserId,
	String email,
	String name,
	String imageUrl
) {
	public static OAuthUserInfo fromKakao(KakaoUserInfoResponse kakaoUserInfo) {
		return new OAuthUserInfo(
			"kakao",
			kakaoUserInfo.getId().toString(),
			kakaoUserInfo.getKakaoAccount().getEmail(),
			kakaoUserInfo.getKakaoAccount().getProfile().getNickname(),
			kakaoUserInfo.getKakaoAccount().getProfile().getProfileImageUrl()
		);
	}
}
