package wisoft.nextframe.schedulereservationticketing.controller.performance;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.common.response.ApiResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.response.PerformanceDetailResponse;
import wisoft.nextframe.schedulereservationticketing.service.performance.PerformanceService;

@RestController
@RequestMapping("/api/v1/performances")
@RequiredArgsConstructor
public class PerformanceController {

	private final PerformanceService performanceService;

	@GetMapping("/{id}")
	public ApiResponse<PerformanceDetailResponse> getPerformanceDetail(@PathVariable UUID id) {
		final PerformanceDetailResponse data = performanceService.getPerformanceDetail(id);

		return ApiResponse.success(data);
	}
}
