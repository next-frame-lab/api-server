package wisoft.nextframe.schedulereservationticketing.ticketing.service;

import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.reservation.domain.ReservationId;
import wisoft.nextframe.schedulereservationticketing.ticketing.entity.Ticket;
import wisoft.nextframe.schedulereservationticketing.ticketing.repository.TicketRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

	private final TicketRepository ticketRepository;

	/**
	 * 예약 기반 티켓 발급
	 * - 지금은 seatId/scheduleId가 없으므로 null 허용
	 * - 중복 발급 방지 멱등성 보장
	 */
	@Transactional
	public Ticket issueByReservation(UUID reservationId) {
		ticketRepository.findByReservationId(reservationId)
			.ifPresent(existing -> {
				throw new AlreadyIssuedException(ReservationId.of(reservationId));
			});

		// TODO: 예약 BC 연동 후 seatId, scheduleId 조회해서 세팅
		// ReservationSnapshot s = reservationQueryPort.fetchSnapshot(reservationId);
		// UUID seatId = s.seatId();
		// UUID scheduleId = s.scheduleId();

		Ticket ticket = Ticket.issue(reservationId);

		// 2. 저장
		try {
			return ticketRepository.save(ticket);
		} catch (DataIntegrityViolationException e) {
			// 멀티 스레드 경쟁 시 유니크 제약으로 중복 저장 실패 가능
			log.warn("중복 발급 경쟁 감지 reservationId={}", reservationId);
			return ticketRepository.findByReservationId(reservationId)
				.orElseThrow(() -> e); // 정말 없으면 원인 재전파
		}
	}
}