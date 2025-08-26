package wisoft.nextframe.schedulereservationticketing.dto.performance.performancedetail.response;

import lombok.Builder;

@Builder
public record SeatSectionPriceResponse(String section, Integer price) {

}
