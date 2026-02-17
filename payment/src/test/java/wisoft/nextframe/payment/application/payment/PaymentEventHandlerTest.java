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
import wisoft.nextframe.payment.application.payment.outbox.cancel.ReservationCancelOutboxService;
import wisoft.nextframe.payment.domain.payment.event.PaymentFailedEvent;

@ExtendWith(MockitoExtension.class)
class PaymentEventHandlerTest {

    @Mock
    ReservationCancelOutboxService reservationCancelOutboxService;

    @InjectMocks
    PaymentEventHandler handler;

    @Test
    @DisplayName("결제 실패 이벤트를 받으면 예약 취소 outbox 서비스를 호출한다")
    void paymentFailedEventTriggersReservationCancel() {
        // given
        UUID paymentId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        PaymentFailedEvent event = new PaymentFailedEvent(paymentId, reservationId);

        // when
        handler.onPaymentFailed(event);

        // then
        verify(reservationCancelOutboxService).cancelOrEnqueue(paymentId, reservationId);
    }
}
