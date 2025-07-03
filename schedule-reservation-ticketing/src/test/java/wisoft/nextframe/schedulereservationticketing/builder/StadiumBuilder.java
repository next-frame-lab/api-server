package wisoft.nextframe.schedulereservationticketing.builder;

import java.util.UUID;

import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;

public class StadiumBuilder {

	private UUID id = UUID.randomUUID();
	private String name = "공연장 이름";
	private String address = "공연장 주소";

	public StadiumBuilder withId(UUID id) {
		this.id = id;
		return this;
	}

	public StadiumBuilder withName(String name) {
		this.name = name;
		return this;
	}

	public Stadium build() {
		return new Stadium(id, name, address);
	}
}
