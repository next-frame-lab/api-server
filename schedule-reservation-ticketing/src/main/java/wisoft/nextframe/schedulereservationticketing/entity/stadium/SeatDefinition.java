package wisoft.nextframe.schedulereservationticketing.entity.stadium;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@Entity
@Table(name = "seat_definitions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeatDefinition {

	@Id
	@Column(name = "id", nullable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stadium_id")
	private Stadium stadium;

	@Column(name = "row_no", nullable = false)
	private Integer rowNo;

	@Column(name = "column_no")
	private Integer columnNo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stadium_section_id")
	private StadiumSection stadiumSection;
}