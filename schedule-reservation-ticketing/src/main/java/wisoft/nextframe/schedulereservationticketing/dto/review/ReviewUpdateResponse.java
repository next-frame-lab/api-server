package wisoft.nextframe.schedulereservationticketing.dto.review;

import java.time.LocalDateTime;
import java.util.UUID;

import wisoft.nextframe.schedulereservationticketing.entity.review.Review;

/**
 * 리뷰 수정 성공 시 응답 데이터를 담는 DTO
 *
 * @param id        수정된 리뷰의 고유 ID
 * @param updatedAt 리뷰 수정 일시
 */
public record ReviewUpdateResponse(
	UUID id,
	LocalDateTime updatedAt
) {
	public static ReviewUpdateResponse from(Review review) {
		return new ReviewUpdateResponse(review.getId(), review.getUpdatedAt());
	}
}
