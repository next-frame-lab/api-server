package wisoft.nextframe.schedulereservationticketing.dto.seatdefinition;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;

@Getter
@Builder
@AllArgsConstructor
public class SeatDefinitionResponse {

	private final UUID id;
	private final String section;
	private final Integer row;
	private final Integer column;

	/**
	 * SeatDefinition 엔티티를 DTO로 변환하는 정적 팩토리 메서드
	 * @param seatDefinition 변환할 엔티티 객체
	 * @return 변환된 DTO 객체
	 */
	public static SeatDefinitionResponse from(SeatDefinition seatDefinition) {
		return SeatDefinitionResponse.builder()
			.id(seatDefinition.getId())
			.section(seatDefinition.getStadiumSection().getSection())
			.row(seatDefinition.getRowNo())
			.column(seatDefinition.getColumnNo())
			.build();
	}
}
