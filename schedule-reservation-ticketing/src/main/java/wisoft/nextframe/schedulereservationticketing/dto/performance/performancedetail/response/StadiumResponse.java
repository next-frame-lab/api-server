package wisoft.nextframe.schedulereservationticketing.dto.performance.performancedetail.response;

import java.util.UUID;

import lombok.Builder;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;

@Builder
public record StadiumResponse(UUID id, String name, String address) {

	public static StadiumResponse from(Stadium stadium) {
		return StadiumResponse.builder()
			.id(stadium.getId())
			.name(stadium.getName())
			.address(stadium.getAddress())
			.build();
	}
}
