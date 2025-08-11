package wisoft.nextframe.schedulereservationticketing.schedule.repository.performance;

import org.springframework.data.jpa.repository.JpaRepository;

import wisoft.nextframe.schedulereservationticketing.schedule.entity.performance.PerformancePricing;
import wisoft.nextframe.schedulereservationticketing.schedule.entity.performance.PerformancePricingId;

public interface PerformancePricingRepository extends JpaRepository<PerformancePricing, PerformancePricingId> {
}