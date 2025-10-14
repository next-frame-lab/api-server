package wisoft.nextframe.schedulereservationticketing.controller.review;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewLikeResponse;
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewListResponse;
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewUpdateRequest;
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewUpdateResponse;
import wisoft.nextframe.schedulereservationticketing.service.review.ReviewService;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {

	private final ReviewService reviewService;

	@PostMapping("/performances/{performanceId}/reviews")
	public ResponseEntity<ApiResponse<?>> createReview(
		@PathVariable UUID performanceId,
		@AuthenticationPrincipal UUID userId,
		@Valid @RequestBody ReviewCreateRequest request
	) {
		final ReviewCreateResponse reviewCreateResponse = reviewService.createReview(performanceId, userId, request);

		final ApiResponse<ReviewCreateResponse> response = ApiResponse.success(reviewCreateResponse);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/performances/{performanceId}/reviews")
	public ResponseEntity<ApiResponse<?>> getReviews(
		@PathVariable UUID performanceId,
		@AuthenticationPrincipal UUID userId,
		@PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		final ReviewListResponse reviewListResponse = reviewService.getReviewsByPerformanceId(
			performanceId,
			userId,
			pageable
		);

		final ApiResponse<ReviewListResponse> response = ApiResponse.success(reviewListResponse);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PatchMapping("/reviews/{reviewId}")
	public ResponseEntity<ApiResponse<ReviewUpdateResponse>> updateReview(
		@PathVariable UUID reviewId,
		@AuthenticationPrincipal UUID userId,
		@Valid @RequestBody ReviewUpdateRequest request
	) {
		final ReviewUpdateResponse reviewUpdateResponse = reviewService.updateReview(reviewId, userId, request);

		final ApiResponse<ReviewUpdateResponse> response = ApiResponse.success(reviewUpdateResponse);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@DeleteMapping("/reviews/{reviewId}")
	public ResponseEntity<Void> deleteReview(
		@PathVariable UUID reviewId,
		@AuthenticationPrincipal UUID userId
	) {
		reviewService.deleteReview(reviewId, userId);

		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@PostMapping("/reviews/{reviewId}/likes")
	public ResponseEntity<ApiResponse<?>> toggleLike(
		@PathVariable UUID reviewId,
		@AuthenticationPrincipal UUID userId
	) {
		final ReviewLikeResponse reviewLikeResponse = reviewService.toggleReviewLike(reviewId, userId);

		final ApiResponse<ReviewLikeResponse> response = ApiResponse.success(reviewLikeResponse);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
