package wisoft.nextframe.schedulereservationticketing.dto.performancedetail.response;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StadiumResponse {

	private final UUID id;
	private final String name;
	private final String address;
}
