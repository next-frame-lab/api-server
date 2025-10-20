package wisoft.nextframe.schedulereservationticketing.controller.performance;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.common.response.ApiResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancedetail.response.PerformanceDetailResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PerformanceListResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.Top10PerformanceListResponse;
import wisoft.nextframe.schedulereservationticketing.service.performance.PerformanceService;

@Slf4j
@RestController
@RequestMapping("/api/v1/performances")
@RequiredArgsConstructor
public class PerformanceController {

	private final PerformanceService performanceService;

	@PreAuthorize("@dynamicAuthService.canViewPerformanceDetail(#id, authentication)")
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<?>> getPerformanceDetail(@P("id") @PathVariable UUID id) {
		log.info("공연 상세 조회 요청. performanceId: {}", id);

		final PerformanceDetailResponse data = performanceService.getPerformanceDetail(id);
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

	@GetMapping("/top10")
	public ResponseEntity<ApiResponse<?>> getTop10Performances() {
		log.info("인기 공연 TOP 10 조회 요청.");
		final Top10PerformanceListResponse data = performanceService.getTop10Performances();

		final ApiResponse<Top10PerformanceListResponse> response = ApiResponse.success(data);
		log.info("인기 공연 TOP 10 조회 성공.");
		return ResponseEntity.ok(response);
	}
}
