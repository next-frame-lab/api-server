package wisoft.nextframe.schedulereservationticketing.service.ticketing;

import java.util.UUID;

public interface PaymentClient {
    boolean isApprovedByReservation(UUID reservationId);
}
