package wisoft.nextframe.payment.infra;

import java.time.LocalDateTime;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.payment.application.payment.port.output.ReservationReader;
import wisoft.nextframe.payment.domain.ReservationId;

@Repository
@RequiredArgsConstructor
public class DbReservationReader implements ReservationReader {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public boolean exists(ReservationId reservationId) {
        Boolean exists = jdbcTemplate.queryForObject(
          "select exists(select 1 from reservations where id = ?)",
          Boolean.class,
          reservationId.value()
        );
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean isPayable(ReservationId reservationId) {
        Boolean payable = jdbcTemplate.queryForObject(
            "select exists("
                + "select 1 from reservations "
                + "where id = ? and status = 'CREATED' and expires_at > now()"
                + ")",
            Boolean.class,
            reservationId.value()
        );
        return Boolean.TRUE.equals(payable);
    }

    @Override
    public LocalDateTime getPerformanceDateTime(ReservationId reservationId) {
        return jdbcTemplate.queryForObject(
            "SELECT s.performance_datetime FROM reservations r "
                + "JOIN schedules s ON r.schedule_id = s.id "
                + "WHERE r.id = ?",
            LocalDateTime.class,
            reservationId.value()
        );
    }
}