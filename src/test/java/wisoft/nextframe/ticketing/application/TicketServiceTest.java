package wisoft.nextframe.ticketing.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import wisoft.nextframe.common.Money;
import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.payment.domain.payment.PaymentId;
import wisoft.nextframe.payment.domain.payment.PaymentStatus;
import wisoft.nextframe.reservation.domain.ReservationId;
import wisoft.nextframe.ticketing.application.port.TicketRepository;
import wisoft.nextframe.ticketing.domain.Ticket;
import wisoft.nextframe.ticketing.infra.TicketMapper;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

	@Mock
	private TicketRepository ticketRepository;

	@Mock
	private TicketMapper ticketMapper;

	@InjectMocks
	private TicketService ticketService;

	@Test
	void should_issue_ticket_and_save_with_fixed_ids() {
		// given
		UUID reservationId = UUID.fromString("11111111-2222-3333-4444-555555555555");
		Payment payment = Payment.reconstruct(
			PaymentId.of(UUID.randomUUID()),
			ReservationId.of(reservationId),
			Money.of(50000),
			LocalDateTime.now(),
			PaymentStatus.SUCCEEDED,
			null
		);

		Ticket expectedTicket = Ticket.issueFrom(payment);

		// repository.save가 호출되면 그대로 ticket 반환한다고 가정
		Mockito.when(ticketRepository.save(
			Mockito.eq(expectedTicket),
			Mockito.eq(reservationId),
			Mockito.any(), // seatId는 현재 임시 값
			Mockito.any()  // scheduleId도 임시 값
		)).thenReturn(expectedTicket);

		// when
		Ticket result = ticketService.issueAndNotify(payment);

		// then
		assertThat(result).isEqualTo(expectedTicket);

		// repository.save가 정확히 한 번 호출되었는지 확인
		Mockito.verify(ticketRepository, times(1)).save(
			Mockito.eq(expectedTicket),
			Mockito.eq(reservationId),
			Mockito.any(),
			Mockito.any()
		);
	}
}
