package wisoft.nextframe.schedulereservationticketing.entity.performance;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.StadiumSection;

@Getter
@Builder
@AllArgsConstructor
@Entity
@Table(name = "performance_pricing")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PerformancePricing {

	@EmbeddedId
	private PerformancePricingId id;

	@MapsId("scheduleId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "schedule_id", nullable = false)
	private Schedule schedule;

	@MapsId("stadiumSectionId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "stadium_section_id", nullable = false)
	private StadiumSection stadiumSection;

	@Column(name = "price", nullable = false)
	private Integer price;
}