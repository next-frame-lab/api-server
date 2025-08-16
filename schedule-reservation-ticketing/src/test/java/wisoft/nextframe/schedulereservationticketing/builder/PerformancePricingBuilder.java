package wisoft.nextframe.schedulereservationticketing.builder;

import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricing;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricingId;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.StadiumSection;

public class PerformancePricingBuilder {

	private Performance performance = new PerformanceBuilder().build();
	private StadiumSection stadiumSection = new StadiumSectionBuilder().build();
	private Integer price = 120000;

	public PerformancePricingBuilder withPerformance(Performance performance) {
		this.performance = performance;
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
		PerformancePricingId id = new PerformancePricingId(performance.getId(), stadiumSection.getId());

		return new PerformancePricing(id, performance, stadiumSection, price);
	}
}
