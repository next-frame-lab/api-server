package wisoft.nextframe.schedulereservationticketing.repository.schedule;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {

	List<Schedule> findByPerformanceId(UUID performanceId);
}
