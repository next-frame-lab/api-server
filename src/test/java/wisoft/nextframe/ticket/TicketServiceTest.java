package wisoft.nextframe.ticket;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wisoft.nextframe.payment.Payment;
import wisoft.nextframe.payment.TestPaymentFactory;

public class TicketServiceTest {

	private final TicketService ticketService = new TicketService();

	@Test
	@DisplayName("결제 상태가 PAID이면 QR 코드가 포함된 티켓이 발급된다")
	void issueQrTicketWhenPaid() {
		Payment payment = mock(Payment.class);
		when(payment.isPaid()).thenReturn(true);

		Ticket ticket = ticketService.issue(payment);

		assertThat(ticket).isNotNull();
		assertThat(ticket.getQrCode()).startsWith("QR-");
	}

	@Test
	@DisplayName("결제 상태가 PAID가 아니면 QR 티켓 발급 시 예외가 발생한다")
	void denyQrTicketIfNotPaid() {
		Payment payment = mock(Payment.class);
		when(payment.isPaid()).thenReturn(false); // 조건 위반

		assertThatThrownBy(() -> ticketService.issue(payment))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("결제 완료된 상태가 아닙니다.");
	}

	@Test
	@DisplayName("QR 코드에 예매 UUID와 티켓 고유 UUID가 포함되어야 한다")
	void createQrCodeWithReservationAndTicketId() {

		Payment payment = TestPaymentFactory.paid();

		Ticket ticket = ticketService.issue(payment);
		String qr = ticket.getQrCode();

		// QR은 "QR-<reservationId>-<ticketUUID>" 형식이어야 한다
		assertThat(qr).startsWith("QR-" + payment.getReservationId() + "-");

		String[] parts = qr.split("-");
		// UUID는 하이픈이 포함된 구조이므로, 전체 QR은 11조각 이상이어야 한다
		assertThat(parts).hasSizeGreaterThanOrEqualTo(11);
	}

}
