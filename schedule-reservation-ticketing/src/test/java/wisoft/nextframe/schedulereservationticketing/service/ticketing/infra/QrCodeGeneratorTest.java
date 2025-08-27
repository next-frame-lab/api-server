package wisoft.nextframe.schedulereservationticketing.service.ticketing.infra;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class QrCodeGeneratorTest {

	private final QrCodeGenerator qrCodeGenerator = new QrCodeGenerator();

	@Test
	@DisplayName("png byte로 생성된다.")
	void generateReturnPngBytes() {
		//when
		byte[] qrBytes = qrCodeGenerator.generate("TEST-QR");

		//then
		assertThat(qrBytes).isNotEmpty();

		assertThat(qrBytes[0]).isEqualTo((byte)0x89); // PNG file signature
		assertThat(qrBytes[1]).isEqualTo((byte)0x50); // P
		assertThat(qrBytes[2]).isEqualTo((byte)0x4E); // N
		assertThat(qrBytes[3]).isEqualTo((byte)0x47); // G

	}

}
