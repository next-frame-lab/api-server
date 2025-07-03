package wisoft.nextframe.schedulereservationticketing.builder;

import java.util.UUID;

import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.StadiumSection;

public class StadiumSectionBuilder {

	private UUID id = UUID.randomUUID();
	private Stadium stadium = new StadiumBuilder().build();
	private String sectionName = "A";

	public StadiumSectionBuilder withStadium(Stadium stadium) {
		this.stadium = stadium;
		return this;
	}

	public StadiumSectionBuilder withId(UUID id) {
		this.id = id;
		return this;
	}

	public StadiumSection build() {
		return new StadiumSection(id, stadium, sectionName);
	}
}
