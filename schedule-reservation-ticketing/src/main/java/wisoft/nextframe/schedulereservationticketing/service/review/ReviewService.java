package wisoft.nextframe.schedulereservationticketing.service.review;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PaginationResponse;
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewCreateRequest;
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewCreateResponse;
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewItemResponse;
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewLikeResponse;
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewListResponse;
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewUpdateRequest;
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewUpdateResponse;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.review.Review;
import wisoft.nextframe.schedulereservationticketing.entity.review.ReviewLike;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.exception.review.DuplicateReviewException;
import wisoft.nextframe.schedulereservationticketing.exception.review.NoReservationFoundException;
import wisoft.nextframe.schedulereservationticketing.exception.review.ReviewPermissionDeniedException;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformanceRepository;
import wisoft.nextframe.schedulereservationticketing.repository.reservation.ReservationRepository;
import wisoft.nextframe.schedulereservationticketing.repository.review.ReviewLikeRepository;
import wisoft.nextframe.schedulereservationticketing.repository.review.ReviewRepository;
import wisoft.nextframe.schedulereservationticketing.repository.user.UserRepository;

@Service
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final UserRepository userRepository;
	private final PerformanceRepository performanceRepository;
	private final ReservationRepository reservationRepository;
	private final ReviewLikeRepository reviewLikeRepository;

	@Transactional
	public ReviewCreateResponse createReview(UUID performanceId, UUID userId, ReviewCreateRequest request) {
		// 1. 엔티티 조회
		final User user = userRepository.findById(userId)
			.orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));
		final Performance performance = performanceRepository.findById(performanceId)
			.orElseThrow(() -> new EntityNotFoundException("공연을 찾을 수 없습니다: " + performanceId));

		// 2. 리뷰 생성 가능 여부 검증
		validateReviewCreation(user, performance);

		// 3. 리뷰 엔티티 생성 및 저장
		final Review review = Review.builder()
			.performance(performance)
			.user(user)
			.star(request.star())
			.content(request.content())
			.build();

		final Review savedReview = reviewRepository.save(review);

		// 4. 응답 DTO 변환 및 반환
		return ReviewCreateResponse.from(savedReview);
	}

	public ReviewListResponse getReviewsByPerformanceId(UUID performanceId, UUID userId, Pageable pageable) {
		// 1. 공연이 존재하는지 확인
		final Performance performance = performanceRepository.findById(performanceId)
			.orElseThrow(() -> new EntityNotFoundException("공연을 찾을 수 없습니다: " + performanceId));

		// 2. 페이징된 리뷰 데이터를 조회
		final Page<ReviewItemResponse> reviewPage = reviewRepository.findReviewsByPerformanceId(
			performanceId,
			userId,
			pageable
		);

		// 3. 조회 결과를 최종 응답 DTO로 변환하여 반환
		return new ReviewListResponse(reviewPage.getContent(), PaginationResponse.from(reviewPage));
	}

	@Transactional
	public ReviewUpdateResponse updateReview(UUID reviewId, UUID userId, ReviewUpdateRequest request) {
		// 1. 리뷰가 존재하는지 확인
		final Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new EntityNotFoundException("해당 리뷰를 찾을 수 없습니다: " + reviewId));

		// 2. 리뷰 작성자와 요청한 사용자가 동일한지 확인
		if (!review.getUser().getId().equals(userId)) {
			throw new ReviewPermissionDeniedException();
		}

		// 3. 리뷰 수정
		review.update(request.star(), request.content());

		// 4. 수정된 정보를 DTO로 변환하여 반환
		return ReviewUpdateResponse.from(review);
	}

	@Transactional
	public void deleteReview(UUID reviewId, UUID userId) {
		// 1. 리뷰가 존재하는지 확인
		final Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new EntityNotFoundException("해당 리뷰를 찾을 수 없습니다: " + reviewId));

		// 2. 권한을 검증합니다. (리뷰 작성자와 현재 로그인한 사용자가 동일한지 확인)
		if (!review.getUser().getId().equals(userId)) {
			throw new ReviewPermissionDeniedException();
		}

		// 3. 권한 검증을 통과하면 리뷰를 물리적으로 삭제합니다.
		reviewRepository.delete(review);
	}

	@Transactional
	public ReviewLikeResponse toggleReviewLike(UUID reviewId, UUID userId) {
		// 1. 필요한 엔티티들 조회
		final Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new EntityNotFoundException("해당 리뷰를 찾을 수 없습니다: " + reviewId));
		final User user = userRepository.findById(userId)
			.orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));

		// 2. 기존 '좋아요' 존재 여부를 확인
		Optional<ReviewLike> existingLike = reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId);

		if (existingLike.isPresent()) {
			// 3-1. 좋아요가 이미 존재하면 -> 취소 로직 실행
			reviewLikeRepository.delete(existingLike.get());
			review.decreaseLikeCount();
			return new ReviewLikeResponse(false); // 최종 상태는 '좋아요 아님'
		} else {
			// 3-2. 좋아요가 없으면 -> 등록 로직 실행
			final ReviewLike newLike = new ReviewLike(review, user);
			reviewLikeRepository.save(newLike);
			review.increaseLikeCount();
			return new ReviewLikeResponse(true); // 최종 상태는 '좋아요'
		}
	}

	private void validateReviewCreation(User user, Performance performance) {
		// 검증 1: 이미 리뷰를 작성했는지 확인
		if (reviewRepository.existsByPerformanceAndUser(performance, user)) {
			throw new DuplicateReviewException();
		}

		// 검증 2: 해당 공연을 예매했는지 확인
		if (!reservationRepository.existsByUserAndPerformance(user, performance)) {
			throw new NoReservationFoundException();
		}
	}
}
