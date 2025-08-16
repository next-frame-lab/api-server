package wisoft.nextframe.schedulereservationticketing.builder;

import java.time.LocalDateTime;
import java.util.UUID;

import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;

public class ScheduleBuilder {

	private UUID id = UUID.randomUUID();
	private Performance performance = new PerformanceBuilder().build();
	private Stadium stadium = new StadiumBuilder().build();
	private LocalDateTime performanceDatetime = LocalDateTime.of(2025, 10, 26, 19, 30);
	private LocalDateTime ticketOpenTime = performanceDatetime.minusWeeks(2); // 공연 2주 전
	private LocalDateTime ticketCloseTime = performanceDatetime.minusHours(1); // 공연 1시간 전

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
