package wisoft.nextframe.schedulereservationticketing.dto.review;

import java.time.LocalDateTime;
import java.util.UUID;

import wisoft.nextframe.schedulereservationticketing.entity.review.Review;

public record ReviewCreateResponse(
	UUID id,
	LocalDateTime createdAt
) {
	public static ReviewCreateResponse from(Review review) {
		return new ReviewCreateResponse(review.getId(), review.getCreatedAt());
	}
}
