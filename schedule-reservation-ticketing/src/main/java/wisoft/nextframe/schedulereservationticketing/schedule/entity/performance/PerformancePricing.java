package wisoft.nextframe.schedulereservationticketing.schedule.entity.performance;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import wisoft.nextframe.schedulereservationticketing.schedule.entity.performance.typeconverter.BigDecimalIntegerConverter;
import wisoft.nextframe.schedulereservationticketing.schedule.entity.stadium.StadiumSection;

@Getter
@Builder
@AllArgsConstructor
@Entity
@Table(name = "performance_pricing")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PerformancePricing {

	@EmbeddedId
	private PerformancePricingId id;

	@MapsId("performanceId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "performance_id", nullable = false)
	private Performance performance;

	@MapsId("stadiumSectionId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "stadium_section_id", nullable = false)
	private StadiumSection stadiumSection;

	@Convert(converter = BigDecimalIntegerConverter.class)
	@Column(name = "price", nullable = false, columnDefinition = "integer")
	private Integer price;
}