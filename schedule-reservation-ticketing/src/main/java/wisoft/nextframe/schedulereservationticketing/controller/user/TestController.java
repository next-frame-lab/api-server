package wisoft.nextframe.schedulereservationticketing.controller.user;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 카카오 소셜 로그인 테스트용 컨트롤러
 */
@Controller
public class TestController {

	// 로그인 페이지
	@GetMapping
	public String loginPage() {
		return "index";
	}

	// 로그인 성공 후 사용자 정보를 보여주는 페이지
	@GetMapping("/user")
	@ResponseBody
	public String userInfo(@AuthenticationPrincipal OAuth2User oAuth2User) {
		if (oAuth2User == null) {
			return "로그인되지 않았습니다.";
		}

		// 사용자 정보를 가져옵니다.
		return "로그인된 사용자 정보: " + oAuth2User.getAttributes();
	}
}
