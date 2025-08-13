package wisoft.nextframe.schedulereservationticketing.repository.performance;

import org.springframework.data.jpa.repository.JpaRepository;

import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricing;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricingId;

public interface PerformancePricingRepository extends JpaRepository<PerformancePricing, PerformancePricingId> {
}