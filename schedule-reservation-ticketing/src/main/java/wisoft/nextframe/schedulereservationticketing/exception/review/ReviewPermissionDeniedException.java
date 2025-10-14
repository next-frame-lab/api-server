package wisoft.nextframe.schedulereservationticketing.exception.review;

public class ReviewPermissionDeniedException extends RuntimeException {
	public ReviewPermissionDeniedException() {
		super("리뷰를 수정할 권한이 없습니다.");
	}
}
