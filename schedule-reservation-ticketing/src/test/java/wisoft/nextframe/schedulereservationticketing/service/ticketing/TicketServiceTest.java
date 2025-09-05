package wisoft.nextframe.schedulereservationticketing.service.ticketing;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import wisoft.nextframe.schedulereservationticketing.dto.ticketing.TicketInfoResponse;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.Reservation;
import wisoft.nextframe.schedulereservationticketing.entity.ticketing.Ticket;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.repository.reservation.ReservationRepository;
import wisoft.nextframe.schedulereservationticketing.repository.ticketing.TicketRepository;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

	@Mock
	TicketRepository ticketRepository;
	@Mock
	ReservationRepository reservationRepository;
	@Mock
	TicketSender ticketSender;

	@InjectMocks
	TicketService ticketService;

	@Test
	@DisplayName("예약 ID로 티켓을 발급하면, 티켓이 저장되고 메일이 발송한다.")
	void issueByReservationSaveTicketAndSendEmail() {
		//given
		UUID reservationId = UUID.randomUUID();

		User user = mock(User.class);
		given(user.getEmail()).willReturn("user@test.com");

		Reservation reservation = mock(Reservation.class);
		given(reservation.getId()).willReturn(reservationId);
		given(reservation.getUser()).willReturn(user);

		given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));
		given(ticketRepository.findByReservationId(reservationId)).willReturn(Optional.empty());

		Ticket ticket = Ticket.issue(reservation);

		TicketInfoResponse response = new TicketInfoResponse(
			ticket.getId(),
			LocalDateTime.now(),
			"QR-DUMMY",
			"테스트 공연",
			1,
			10
		);

		given(ticketRepository.findTicketInfoById(ticket.getId()))
			.willReturn(Optional.of(response));

		//when
		Ticket result = ticketService.issueByReservation(reservationId);

		//then
		assertThat(result).isNotNull();
		verify(ticketRepository).saveAndFlush(any(Ticket.class));
		verify(ticketSender).send(eq(response), eq("user@test.com"));
	}
}
