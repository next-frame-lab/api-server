package wisoft.nextframe.schedulereservationticketing.repository.review;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.review.Review;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

	/**
	 * 특정 공연과 사용자에 해당하는 리뷰가 존재하는지 확인합니다.
	 * @param performance 공연 엔티티
	 * @param user 사용자 엔티티
	 * @return 리뷰 존재 여부 (true/false)
	 */
	boolean existsByPerformanceAndUser(Performance performance, User user);
}
