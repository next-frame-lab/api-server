package wisoft.nextframe.schedulereservationticketing.repository.schedule;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {
}