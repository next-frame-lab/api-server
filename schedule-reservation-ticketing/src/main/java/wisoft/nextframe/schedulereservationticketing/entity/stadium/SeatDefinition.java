package wisoft.nextframe.schedulereservationticketing.entity.stadium;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@Entity
@Table(
	name = "seat_definitions",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uq_seat_definitions_location",
			columnNames = {"stadium_section_id", "row_no", "column_no"}
		)
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeatDefinition {

	@Id
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(name = "row_no", nullable = false)
	private Integer rowNo;

	@Column(name = "column_no", nullable = false)
	private Integer columnNo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stadium_section_id", nullable = false)
	private StadiumSection stadiumSection;
}