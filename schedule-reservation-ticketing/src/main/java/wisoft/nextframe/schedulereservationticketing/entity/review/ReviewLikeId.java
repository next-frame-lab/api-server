package wisoft.nextframe.schedulereservationticketing.entity.review;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReviewLikeId implements Serializable {

	@Column(name = "review_id")
	private UUID reviewId;

	@Column(name = "user_id")
	private UUID userId;
}
