package wisoft.nextframe.schedulereservationticketing.builder;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.StadiumSection;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SeatDefinitionBuilder {

	private UUID id = UUID.randomUUID();
	private Integer rowNo = 1;
	private Integer columnNo = 1;
	private StadiumSection stadiumSection;

	public static SeatDefinitionBuilder builder() {
		return new SeatDefinitionBuilder();
	}

	public SeatDefinitionBuilder withId(UUID id) {
		this.id = id;
		return this;
	}

	public SeatDefinitionBuilder withRowNo(Integer rowNo) {
		this.rowNo = rowNo;
		return this;
	}

	public SeatDefinitionBuilder withColumnNo(Integer columnNo) {
		this.columnNo = columnNo;
		return this;
	}

	public SeatDefinitionBuilder withStadiumSection(StadiumSection stadiumSection) {
		this.stadiumSection = stadiumSection;
		return this;
	}

	public SeatDefinition build() {
		if (stadiumSection == null) {
			throw new IllegalStateException("StadiumSection is required for SeatDefinitionBuilder.");
		}

		return new SeatDefinition(id, rowNo, columnNo, stadiumSection);
	}
}
