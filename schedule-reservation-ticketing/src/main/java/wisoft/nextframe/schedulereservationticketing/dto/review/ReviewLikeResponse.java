package wisoft.nextframe.schedulereservationticketing.dto.review;

/**
 * 리뷰 좋아요 처리 후의 응답 데이터를 담는 DTO
 *
 * @param likeStatus 최종 좋아요 상태 (true: 좋아요, false: 좋아요 취소)
 */
public record ReviewLikeResponse(
	boolean likeStatus
) {
}
