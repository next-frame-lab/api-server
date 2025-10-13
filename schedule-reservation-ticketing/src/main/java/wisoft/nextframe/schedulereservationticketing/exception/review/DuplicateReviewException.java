package wisoft.nextframe.schedulereservationticketing.exception.review;

public class DuplicateReviewException extends ReviewException {
	public DuplicateReviewException() {
		super("이미 해당 공연에 대한 리뷰를 작성했습니다.");
	}
}
