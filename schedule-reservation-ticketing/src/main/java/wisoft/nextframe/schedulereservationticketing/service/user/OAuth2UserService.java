package wisoft.nextframe.schedulereservationticketing.service.user;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.repository.user.UserRepository;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

	private final UserRepository userRepository;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		// 부모 클래스의 loadUser 메서드를 호출하여 OAuth2 사용자의 정보를 가져옵니다.
		final OAuth2User oAuth2User = super.loadUser(userRequest);

		// OAuth 공급자(provider)로부터 받은 사용자 속성을 가져옵니다.
		final Map<String, Object> attributes = oAuth2User.getAttributes();

		// 사용자 속성에서 kakao_account와 profile을 추출합니다.
		final Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
		final Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

		final String email = (String) kakaoAccount.get("email");
		final String name = (String) profile.get("nickname");

		// 이메일을 기반으로 데이터베이스에서 사용자를 찾거나 새로 생성합니다.
		final User user = saveOrUpdate(email, name);

		// email 기반으로 데이터베이스에서 사용자를 찾거나 객체를 반환합니다.
		return new DefaultOAuth2User(
			Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
			attributes,
			"id"
		);
	}

	/**
	 * 데이터베이스에 사용자가 없으면 새로 저장하고, 있으면 기존 정보를 반환합니다.
	 * @param email 사용자 이메일
	 * @param name 사용자 이름
	 * @return 저장되거나 조회된 User 엔티티
	 */
	private User saveOrUpdate(String email, String name) {
		final Optional<User> userOptional = userRepository.findByEmail(email);

		User user;

		if (userOptional.isPresent()) {
			// 이미 존재하는 경우, 그대로 반환합니다.
			user = userOptional.get();
		} else {
			// 새로운 사용자인 경우, 엔티티를 생성하여 저장합니다.
			user = User.builder()
				.email(email)
				.name(name)
				.birthDate(LocalDate.of(1998, 4, 7))
				.phoneNumber("010-0000-0000")
				.build();
			userRepository.save(user);
		}

		return user;
	}
}
