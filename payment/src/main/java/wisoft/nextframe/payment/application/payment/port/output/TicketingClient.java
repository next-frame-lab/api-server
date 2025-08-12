// payment/service/port/TicketingClient.java
package wisoft.nextframe.payment.application.payment.port.output;

import java.util.UUID;

public interface TicketingClient {
    void issueTicket(UUID reservationId);
}
