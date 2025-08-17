package wisoft.nextframe.schedulereservationticketing.controller.performance;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.common.response.ApiResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performancedetail.response.PerformanceDetailResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performancelist.response.PerformanceListResponse;
import wisoft.nextframe.schedulereservationticketing.service.performance.PerformanceService;

@RestController
@RequestMapping("/api/v1/performances")
@RequiredArgsConstructor
public class PerformanceController {

	private final PerformanceService performanceService;

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<?>> getPerformanceDetail(@PathVariable UUID id) {
		final PerformanceDetailResponse data = performanceService.getPerformanceDetail(id);

		final ApiResponse<PerformanceDetailResponse> response = ApiResponse.success(data);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping
	public ResponseEntity<ApiResponse<?>> getPerformances(@PageableDefault(size = 32) Pageable pageable) {
		final PerformanceListResponse data = performanceService.getReservablePerformances(pageable);

		final ApiResponse<PerformanceListResponse> response = ApiResponse.success(data);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
