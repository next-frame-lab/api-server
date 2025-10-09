package wisoft.nextframe.schedulereservationticketing.repository.review;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewItemResponse;
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

	/**
	 * 특정 공연에 대한 리뷰 목록을 페이징하여 조회합니다.
	 * JPQL을 사용하여 리뷰, 작성자(User), 현재 사용자의 좋아요(ReviewLike) 정보를 한 번에 가져옵니다.
	 *
	 * @param performanceId 조회할 공연의 ID
	 * @param userId        현재 로그인한 사용자의 ID (비로그인 시 null)
	 * @param pageable      페이징 및 정렬 정보 (예: 최신순, 좋아요순)
	 * @return 페이징된 ReviewItem DTO 목록
	 */
	@Query(value = """
        SELECT new wisoft.nextframe.schedulereservationticketing.dto.review.ReviewItemResponse(
            r.id,
            r.user.name,
            r.user.imageUrl,
            r.content,
            CASE WHEN rl.id IS NOT NULL THEN true ELSE false END,
            r.likeCount,
            r.createdAt,
            r.updatedAt
        )
        FROM Review r
        JOIN r.user
        LEFT JOIN ReviewLike rl ON rl.review = r AND rl.user.id = :userId
        WHERE r.performance.id = :performanceId
        ORDER BY r.createdAt DESC
    """,
		countQuery = """
        SELECT COUNT(r)
        FROM Review r
        WHERE r.performance.id = :performanceId
    """)
	Page<ReviewItemResponse> findReviewsByPerformanceId(
		@Param("performanceId") UUID performanceId,
		@Param("userId") UUID userId,
		Pageable pageable
	);
}
