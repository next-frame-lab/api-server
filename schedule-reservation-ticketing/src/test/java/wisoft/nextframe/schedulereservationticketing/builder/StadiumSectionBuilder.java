package wisoft.nextframe.schedulereservationticketing.builder;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.StadiumSection;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StadiumSectionBuilder {

	private UUID id = UUID.randomUUID();
	private Stadium stadium;
	private String section = "A";

	public static StadiumSectionBuilder builder() {
		return new StadiumSectionBuilder();
	}

	public StadiumSectionBuilder withStadium(Stadium stadium) {
		this.stadium = stadium;
		return this;
	}

	public StadiumSectionBuilder withSectionName(String section) {
		this.section = section;
		return this;
	}

	public StadiumSection build() {
		return new StadiumSection(id, stadium, section);
	}
}
