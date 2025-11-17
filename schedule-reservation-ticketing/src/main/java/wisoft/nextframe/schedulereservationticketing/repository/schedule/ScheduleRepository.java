package wisoft.nextframe.schedulereservationticketing.repository.schedule;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {

	@Query("""
		select s from Schedule s
		join fetch s.performance
		where s.id = :id
		""")
	Schedule findWithPerformanceById(UUID id);

	List<Schedule> findByPerformanceId(UUID performanceId);
}
