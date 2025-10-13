package wisoft.nextframe.schedulereservationticketing.controller.review;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.common.response.ApiResponse;
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewCreateRequest;
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewCreateResponse;
import wisoft.nextframe.schedulereservationticketing.service.review.ReviewService;

@RestController
@RequestMapping("/api/v1/performances")
@RequiredArgsConstructor
public class ReviewController {

	private final ReviewService reviewService;

	@PostMapping("/{performanceId}/reviews")
	public ResponseEntity<ApiResponse<?>> createReview(
		@PathVariable UUID performanceId,
		@AuthenticationPrincipal UUID userId,
		@Valid @RequestBody ReviewCreateRequest request
	) {
		final ReviewCreateResponse reviewCreateResponse = reviewService.createReview(performanceId, userId, request);

		final ApiResponse<ReviewCreateResponse> response = ApiResponse.success(reviewCreateResponse);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
