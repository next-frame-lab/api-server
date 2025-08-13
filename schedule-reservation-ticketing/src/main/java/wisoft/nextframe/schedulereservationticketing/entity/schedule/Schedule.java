package wisoft.nextframe.schedulereservationticketing.entity.schedule;

import java.time.LocalDateTime;
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
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;

@Getter
@Builder
@AllArgsConstructor
@Entity
@Table(name = "schedules")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule {

	@Id
	@Column(name = "id", nullable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "performance_id", nullable = false)
	private Performance performance;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stadium_id", nullable = false)
	private Stadium stadium;

	@Column(name = "performance_datetime")
	private LocalDateTime performanceDatetime;

	@Column(name = "ticket_open_time")
	private LocalDateTime ticketOpenTime;

	@Column(name = "ticket_close_time")
	private LocalDateTime ticketCloseTime;
}