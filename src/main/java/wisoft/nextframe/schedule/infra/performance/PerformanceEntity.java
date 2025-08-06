package wisoft.nextframe.schedule.infra.performance;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "performances")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class PerformanceEntity {

	@Id
	private UUID id;

	private String name;

	private String genre;

	private String type;

	@Column(name = "adult_only")
	private boolean adultOnly;

	@Column(name = "running_time")
	private Integer runningTime;

	@Column(name = "image_url")
	private String imageUrl;

	@Column(columnDefinition = "text")
	private String description;

}

