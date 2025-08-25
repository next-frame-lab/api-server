package wisoft.nextframe.schedulereservationticketing.builder;

import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricing;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricingId;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.StadiumSection;

public class PerformancePricingBuilder {

	private Schedule schedule = new ScheduleBuilder().build();
	private StadiumSection stadiumSection = new StadiumSectionBuilder().build();
	private Integer price = 120000;

	public PerformancePricingBuilder withSchedule(Schedule schedule) {
		this.schedule = schedule;
		return this;
	}

	public PerformancePricingBuilder withStadiumSection(StadiumSection stadiumSection) {
		this.stadiumSection = stadiumSection;
		return this;
	}

	public PerformancePricing build() {
		PerformancePricingId id = new PerformancePricingId(schedule.getId(), stadiumSection.getId());

		return new PerformancePricing(id, schedule, stadiumSection, price);
	}
}
