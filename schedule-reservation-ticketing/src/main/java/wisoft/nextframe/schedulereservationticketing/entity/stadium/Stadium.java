package wisoft.nextframe.schedulereservationticketing.entity.stadium;

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
@Builder
@AllArgsConstructor
@Entity
@Table(name = "stadiums")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stadium {

	@Id
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(name = "name")
	private String name;

	@Column(name = "address")
	private String address;
}