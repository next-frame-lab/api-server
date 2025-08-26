package wisoft.nextframe.schedulereservationticketing.dto.performance.performancedetail.response;

import java.util.UUID;

import lombok.Builder;

@Builder
public record StadiumResponse(UUID id, String name, String address) {
}
