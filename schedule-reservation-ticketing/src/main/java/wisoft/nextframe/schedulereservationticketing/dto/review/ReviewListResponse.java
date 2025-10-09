package wisoft.nextframe.schedulereservationticketing.dto.review;

import java.util.List;

import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PaginationResponse;

/**
 * 리뷰 목록 조회 API의 최종 응답 DTO
 *
 * @param reviews      리뷰 목록
 * @param pagination   페이지네이션 정보
 */
public record ReviewListResponse(
	List<ReviewItemResponse> reviews,
	PaginationResponse pagination
) {
}
