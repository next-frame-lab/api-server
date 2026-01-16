package wisoft.nextframe.payment.infra;

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
}