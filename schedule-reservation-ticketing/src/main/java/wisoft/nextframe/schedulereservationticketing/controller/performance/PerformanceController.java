package wisoft.nextframe.schedulereservationticketing.controller.performance;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.common.response.ApiResponse;
import wisoft.nextframe.schedulereservationticketing.config.jwt.JwtTokenProvider;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancedetail.response.PerformanceDetailResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PerformanceListResponse;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.repository.user.UserRepository;
import wisoft.nextframe.schedulereservationticketing.service.performance.PerformanceService;

@Slf4j
@RestController
@RequestMapping("/api/v1/performances")
@RequiredArgsConstructor
public class PerformanceController {

	private final PerformanceService performanceService;
	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<?>> getPerformanceDetail(@PathVariable UUID id, HttpServletRequest request) {
		log.info("공연 상세 조회 요청. performanceId: {}", id);
		final PerformanceDetailResponse data = performanceService.getPerformanceDetail(id);

		if (data.adultOnly()) {
			log.info("성인 전용 공연. 인증 확인 시작. performanceId: {}", id);
			final String token = resolveToken(request);

			if (token == null || !jwtTokenProvider.validateToken(token)) {
				log.warn("유효하지 않은 토큰으로 성인 공연 접근 시도. performanceId: {}", id);
				throw new AuthenticationCredentialsNotFoundException("유효한 인증 정보가 없습니다.");
			}

			final UUID userId = jwtTokenProvider.getUserIdFromToken(token);
			final User user = userRepository.findById(userId)
				.orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다."));

			if (!user.isAdult()) {
				log.warn("성인 인증 실패. userId: {}, performanceId: {}", userId, id);
				throw new AccessDeniedException("성인 인증이 필요한 공연입니다.");
			}
			log.info("성인 인증 성공. userId: {}", userId);
		}

		final ApiResponse<PerformanceDetailResponse> response = ApiResponse.success(data);
		log.info("공연 상세 조회 성공. performanceId: {}", id);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping
	public ResponseEntity<ApiResponse<?>> getPerformanceList(@PageableDefault(size = 32) Pageable pageable) {
		log.info("공연 목록 조회 요청. page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
		final PerformanceListResponse data = performanceService.getPerformanceList(pageable);

		final ApiResponse<PerformanceListResponse> response = ApiResponse.success(data);
		log.info("공연 목록 조회 성공. 반환된 공연 수: {}", data.performances().size());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	private String resolveToken(HttpServletRequest request) {
		final String bearerToken = request.getHeader("Authorization");
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}
}
