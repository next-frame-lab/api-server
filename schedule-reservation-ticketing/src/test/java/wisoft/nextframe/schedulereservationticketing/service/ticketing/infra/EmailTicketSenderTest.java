package wisoft.nextframe.schedulereservationticketing.service.ticketing.infra;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import jakarta.mail.internet.MimeMessage;
import wisoft.nextframe.schedulereservationticketing.dto.ticketing.TicketInfoResponse;

@ExtendWith(MockitoExtension.class)
public class EmailTicketSenderTest {

	@Mock
	JavaMailSender mailSender;

	@Mock
	QrCodeGenerator qrCodeGenerator;

	@InjectMocks
	EmailTicketSender emailTicketSender;

	@Test
	@DisplayName("java mail sender 호출한다.")
	void sendInvokeJavaMailSender() {
		//given
		TicketInfoResponse response = new TicketInfoResponse(
			UUID.randomUUID(),
			LocalDateTime.now(),
			"QR-CODE-TEXT",
			"햄릿",
			1,
			12
		);

		// QR 코드 생성 모킹
		given(qrCodeGenerator.generate(any())).willReturn("dummy".getBytes());

		MimeMessage message = new JavaMailSenderImpl().createMimeMessage();
		given(mailSender.createMimeMessage()).willReturn(message);

		//when
		emailTicketSender.send(response, "user@test.com");

		//then
		verify(mailSender).send(any(MimeMessage.class));
	}
}
