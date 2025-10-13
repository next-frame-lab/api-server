package wisoft.nextframe.schedulereservationticketing.service.review;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewCreateRequest;
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewCreateResponse;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.review.Review;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.exception.review.DuplicateReviewException;
import wisoft.nextframe.schedulereservationticketing.exception.review.NoReservationFoundException;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformanceRepository;
import wisoft.nextframe.schedulereservationticketing.repository.reservation.ReservationRepository;
import wisoft.nextframe.schedulereservationticketing.repository.review.ReviewRepository;
import wisoft.nextframe.schedulereservationticketing.repository.user.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final UserRepository userRepository;
	private final PerformanceRepository performanceRepository;
	private final ReservationRepository reservationRepository;

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
