package wisoft.nextframe.ticket;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wisoft.nextframe.payment.Payment;

public class TicketTest {

	@Test
	@DisplayName("티켓 발급 시 QR 코드가 생성되고 발급 시각이 기록된다")
	void createTicketWithQrAndIssuedAt() {
		Payment payment = mock(Payment.class);

		Ticket ticket = Ticket.issueFrom(payment);

		assertThat(ticket.getQrCode()).startsWith("QR-");
		assertThat(ticket.getIssuedAt()).isNotNull();
	}

}
