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
	name = "stadium_sections",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "stadium_sections_stadium_id_section_key",
			columnNames = {"stadium_id", "section"}
		)
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StadiumSection {

	@Id
	@Column(name = "id", nullable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "stadium_id", nullable = false)
	private Stadium stadium;

	@Column(name = "section", length = 100, nullable = false)
	private String section;
}