package wisoft.nextframe.schedulereservationticketing.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricing;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricingId;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.StadiumSection;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PerformancePricingBuilder {

	private Schedule schedule;
	private StadiumSection stadiumSection;
	private Integer price = 120000;

	public static PerformancePricingBuilder builder() {
		return new PerformancePricingBuilder();
	}

	public PerformancePricingBuilder withSchedule(Schedule schedule) {
		this.schedule = schedule;
		return this;
	}

	public PerformancePricingBuilder withStadiumSection(StadiumSection stadiumSection) {
		this.stadiumSection = stadiumSection;
		return this;
	}

	public PerformancePricingBuilder withPrice(Integer price) {
		this.price = price;
		return this;
	}

	public PerformancePricing build() {
		if (schedule == null || stadiumSection == null) {
			throw new IllegalStateException("Schedule and StadiumSection are required for PerformancePricingBuilder.");
		}

		PerformancePricingId id = new PerformancePricingId(schedule.getId(), stadiumSection.getId());
		return new PerformancePricing(id, schedule, stadiumSection, price);
	}
}
