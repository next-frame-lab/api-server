package wisoft.nextframe.schedulereservationticketing.schedule.repository.performance;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import wisoft.nextframe.schedulereservationticketing.schedule.entity.performance.Performance;

public interface PerformanceRepository extends JpaRepository<Performance, UUID> {
}