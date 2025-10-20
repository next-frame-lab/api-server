package wisoft.nextframe.schedulereservationticketing.config.security;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.common.response.ApiResponse;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
		AccessDeniedException accessDeniedException) throws IOException, ServletException {
		// 응답 내용 생성
		final ApiResponse<?> apiErrorResponse = ApiResponse.error(ErrorCode.ACCESS_DENIED);

		// HTTP 상태 코드 설정
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);

		// 응답의 Content-Type과 인코딩 설정
		response.setContentType("application/json");
		response.setCharacterEncoding("utf-8");

		// ObjectMapper를 사용해 apiErrorResponse 객체를 Json 문자열로 변환하여 응답 본문에 작성
		final String jsonResponse = objectMapper.writeValueAsString(apiErrorResponse);
		response.getWriter().write(jsonResponse);
	}
}
