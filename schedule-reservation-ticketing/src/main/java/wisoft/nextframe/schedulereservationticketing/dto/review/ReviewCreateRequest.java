package wisoft.nextframe.schedulereservationticketing.dto.review;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewCreateRequest(
	@NotNull(message = "별점은 필수입니다.")
	@DecimalMin(value = "0.5", message = "별점은 0.5 이상이어야 합니다.")
	@DecimalMax(value = "5.0", message = "별점은 5.0 이하이어야 합니다.")
	BigDecimal star,

	@NotBlank(message = "리뷰 내용은 비워둘 수 없습니다.")
	@Size(max = 1000, message = "리뷰 내용은 최대 1000자까지 입력할 수 있습니다.")
	String content
) {
}
