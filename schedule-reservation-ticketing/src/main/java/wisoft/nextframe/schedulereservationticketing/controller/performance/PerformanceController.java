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
		final PerformanceDetailResponse data = performanceService.getPerformanceDetail(id);

		final ApiResponse<PerformanceDetailResponse> response = ApiResponse.success(data);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping
	public ResponseEntity<ApiResponse<?>> getPerformanceList(@PageableDefault(size = 32) Pageable pageable) {
		final PerformanceListResponse data = performanceService.getPerformanceList(pageable);

		final ApiResponse<PerformanceListResponse> response = ApiResponse.success(data);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/top10")
	public ResponseEntity<ApiResponse<?>> getTop10Performances() {
		final Top10PerformanceListResponse data = performanceService.getTop10Performances();

		final ApiResponse<Top10PerformanceListResponse> response = ApiResponse.success(data);

		return ResponseEntity.ok(response);
	}
}
