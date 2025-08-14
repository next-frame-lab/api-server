package wisoft.nextframe.schedulereservationticketing.dto.performance.response;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StadiumResponseDto {

	private final UUID id;
	private final String name;
	private final String address;
}
