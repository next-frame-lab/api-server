package wisoft.nextframe.schedulereservationticketing.ticketing.service;

import java.util.UUID;

public interface PaymentClient {
    boolean isApprovedByReservation(UUID reservationId);
}
