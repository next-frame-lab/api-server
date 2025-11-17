package wisoft.nextframe.schedulereservationticketing.builder;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ScheduleBuilder {

	private UUID id = UUID.randomUUID();
	private Performance performance;
	private Stadium stadium;
	private LocalDateTime performanceDatetime = LocalDateTime.now().plusDays(20);
	private LocalDateTime ticketOpenTime = LocalDateTime.now().minusDays(10);
	private LocalDateTime ticketCloseTime = LocalDateTime.now().plusDays(10);

	public static ScheduleBuilder builder() {
		return new ScheduleBuilder();
	}

	public ScheduleBuilder withId(UUID id) {
		this.id = id;
		return this;
	}

	public ScheduleBuilder withPerformance(Performance performance) {
		this.performance = performance;
		return this;
	}

	public ScheduleBuilder withStadium(Stadium stadium) {
		this.stadium = stadium;
		return this;
	}

	public ScheduleBuilder withPerformanceDatetime(LocalDateTime performanceDatetime) {
		this.performanceDatetime = performanceDatetime;
		return this;
	}

	public ScheduleBuilder withTicketOpenTime(LocalDateTime ticketOpenTime) {
		this.ticketOpenTime = ticketOpenTime;
		return this;
	}

	public ScheduleBuilder withTicketCloseTime(LocalDateTime ticketCloseTime) {
		this.ticketCloseTime = ticketCloseTime;
		return this;
	}

	public Schedule build() {
		return new Schedule(id, performance, stadium, performanceDatetime, ticketOpenTime, ticketCloseTime);
	}
}
