package wisoft.nextframe.schedulereservationticketing.exception.review;

public class NoReservationFoundException extends ReviewException {
    public NoReservationFoundException() {
        super("리뷰를 작성하려면 먼저 공연을 예매해야 합니다.");
    }
}