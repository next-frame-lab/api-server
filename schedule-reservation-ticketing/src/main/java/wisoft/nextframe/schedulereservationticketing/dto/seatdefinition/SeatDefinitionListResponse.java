package wisoft.nextframe.schedulereservationticketing.dto.seatdefinition;

import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;

@Getter
@Builder
@AllArgsConstructor
public class SeatDefinitionListResponse {

	private final List<SeatDefinitionResponse> seats;

	/**
	 * SeatDefinition 엔티티 리스트를 DTO로 변환하는 정적 팩토리 메서드
	 * @param seatDefinitions 변환할 엔티티 리스트
	 * @return 변환된 DTO 객체
	 */
	public static SeatDefinitionListResponse from(List<SeatDefinition> seatDefinitions) {
		List<SeatDefinitionResponse> seatDtoList = seatDefinitions.stream()
			.map(SeatDefinitionResponse::from) // 위에서 만든 DTO의 변환 메서드 사용
			.collect(Collectors.toList());

		return SeatDefinitionListResponse.builder()
			.seats(seatDtoList)
			.build();
	}
}
