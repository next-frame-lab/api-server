package wisoft.nextframe.schedulereservationticketing.dto.review;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 리뷰 목록의 개별 항목을 위한 DTO
 *
 * @param id                  	리뷰 고유 ID
 * @param writerName          	작성자 이름
 * @param writerProfileImageUrl 작성자 프로필 이미지 URL
 * @param content             	리뷰 내용
 * @param star                	사용자가 매긴 공연 별점
 * @param likeStatus          	현재 사용자의 좋아요 여부
 * @param likeCount           	좋아요 개수
 * @param createdAt           	생성 일시
 * @param updatedAt           	수정 일시
 */
public record ReviewItemResponse(
	UUID id,
	String writerName,
	String writerProfileImageUrl,
	String content,
	BigDecimal star,
	boolean likeStatus,
	int likeCount,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
}
