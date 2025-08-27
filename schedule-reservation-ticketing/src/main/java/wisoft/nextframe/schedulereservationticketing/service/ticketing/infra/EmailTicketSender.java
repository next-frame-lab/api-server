package wisoft.nextframe.schedulereservationticketing.service.ticketing.infra;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.dto.ticketing.TicketInfoResponse;
import wisoft.nextframe.schedulereservationticketing.service.ticketing.TicketSender;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailTicketSender implements TicketSender {

	// todo log로 바꾸기
	private final JavaMailSender mailSender;
	private final QrCodeGenerator qrCodeGenerator;

	@Override
	public void send(TicketInfoResponse ticketInfo, String recipientEmail) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = getMimeMessageHelper(ticketInfo, recipientEmail, message);

			byte[] qrImage = qrCodeGenerator.generate(ticketInfo.qrCode());

			helper.addInline("qrCodeImage", new ByteArrayResource(qrImage), "image/png");

			mailSender.send(message);
			log.info("✅ 이메일 전송 성공 to: {}", recipientEmail);

		} catch (Exception e) {
			log.error("❌ 이메일 전송 실패 to: {}", recipientEmail);
			throw new RuntimeException("이메일 발송 실패", e);
		}
	}

	private static MimeMessageHelper getMimeMessageHelper(
		TicketInfoResponse ticketInfo,
		String recipientEmail,
		MimeMessage message
	) throws MessagingException {
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

		helper.setTo(recipientEmail);
		helper.setSubject("🎫 티켓 발급 완료: " + ticketInfo.performanceName());
		String htmlContent = """
			<h1>티켓이 발급되었습니다!</h1>
			<p>공연: %s</p>
			<p>좌석: %s</p>
			<p>발급 시각: %s</p>
			<p>QR 코드를 제시해 입장해주세요.</p>
			<img src="cid:qrCodeImage" alt="QR Code"/>
			""".formatted(
			ticketInfo.performanceName(),
			ticketInfo.seatNumber(),
			ticketInfo.issuedAt()
		);

		helper.setText(htmlContent, true);
		return helper;
	}
}
