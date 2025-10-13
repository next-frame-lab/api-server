package wisoft.nextframe.schedulereservationticketing.repository.review;

import org.springframework.data.jpa.repository.JpaRepository;

import wisoft.nextframe.schedulereservationticketing.entity.review.ReviewLike;
import wisoft.nextframe.schedulereservationticketing.entity.review.ReviewLikeId;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, ReviewLikeId> {
}
