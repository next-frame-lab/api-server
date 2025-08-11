package wisoft.nextframe.ticketing.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.ticketing.application.port.TicketRepository;
import wisoft.nextframe.ticketing.domain.Ticket;
import wisoft.nextframe.ticketing.infra.TicketMapper;

@Service
@RequiredArgsConstructor
public class TicketService {

	private final TicketMapper ticketMapper;
	private final TicketRepository ticketRepository;

	public Ticket issueAndNotify(Payment payment) {
		// 1. 예약 ID만으로 티켓 발급 (도메인 생성)
		Ticket ticket = Ticket.issueFrom(payment);

		// TODO: ReservationRepository 구현 후 실제 seatId, scheduleId 조회로 변경
		// 2. ReservationRepository가 아직 없으므로 임시 seat/schedule 값 사용
		UUID reservationId = payment.getReservationId().getValue();
		UUID fakeSeatId = UUID.fromString("61c8ecf9-4827-4054-9f7a-e0a3db18f32e");
		UUID fakeScheduleId = UUID.fromString("f1000000-aaaa-bbbb-cccc-000000000001");

		// 2. 저장
		ticketRepository.save(ticket, reservationId, fakeSeatId, fakeScheduleId);

		return ticket;
	}
}
