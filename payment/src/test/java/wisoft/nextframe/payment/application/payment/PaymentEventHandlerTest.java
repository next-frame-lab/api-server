package wisoft.nextframe.payment.application.payment;

import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import wisoft.nextframe.payment.application.payment.handler.PaymentEventHandler;
import wisoft.nextframe.payment.application.ticketissue.TicketIssueOutboxService;
import wisoft.nextframe.payment.domain.payment.event.PaymentApprovedEvent;

@ExtendWith(MockitoExtension.class)
class PaymentEventHandlerTest {

    @Mock
    TicketIssueOutboxService outboxService;

    @InjectMocks
    PaymentEventHandler handler;

    @Test
    @DisplayName("이벤트를 받으면 outboxService 호출")
    void paymentApprovedEventTriggersTicketIssue() {
        // given
        UUID paymentId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        PaymentApprovedEvent event =
            new PaymentApprovedEvent(paymentId, reservationId);

        // when
        handler.onPaymentApproved(event);

        // then
        verify(outboxService).issueOrEnqueue(paymentId, reservationId);
    }
}
