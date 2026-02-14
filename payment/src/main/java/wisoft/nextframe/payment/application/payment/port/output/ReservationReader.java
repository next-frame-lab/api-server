package wisoft.nextframe.payment.application.payment.port.output;

import java.time.LocalDateTime;

import wisoft.nextframe.payment.domain.ReservationId;

public interface ReservationReader {
    boolean exists(ReservationId reservationId);

    LocalDateTime getPerformanceDateTime(ReservationId reservationId);
}
