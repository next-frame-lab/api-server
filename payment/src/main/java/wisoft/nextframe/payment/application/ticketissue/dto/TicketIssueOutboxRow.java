package wisoft.nextframe.payment.application.ticketissue.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketIssueOutboxRow(
    UUID reservationId,
    UUID paymentId,
    UUID ticketId,
    String status,
    int retryCount,
    LocalDateTime nextRetryAt
) {
}