package wisoft.nextframe.schedulereservationticketing.repository.performance;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;

public interface PerformanceRepository extends JpaRepository<Performance, UUID> {
}