package wisoft.nextframe.schedulereservationticketing.repository.review;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import wisoft.nextframe.schedulereservationticketing.entity.review.ReviewLike;
import wisoft.nextframe.schedulereservationticketing.entity.review.ReviewLikeId;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, ReviewLikeId> {

	/**
	 * 특정 리뷰와 사용자에 해당하는 좋아요 정보를 조회합니다.
	 */
	Optional<ReviewLike> findByReviewIdAndUserId(UUID reviewId, UUID userId);
}
