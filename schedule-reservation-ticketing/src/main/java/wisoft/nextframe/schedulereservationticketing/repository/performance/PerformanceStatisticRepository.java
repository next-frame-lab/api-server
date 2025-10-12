package wisoft.nextframe.schedulereservationticketing.repository.performance;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceStatistic;

public interface PerformanceStatisticRepository extends JpaRepository<PerformanceStatistic, UUID> {
}
