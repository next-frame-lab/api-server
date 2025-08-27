package wisoft.nextframe.schedulereservationticketing.service.ticketing.infra;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.stereotype.Component;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

@Component
public class QrCodeGenerator {

	public byte[] generate(String qrText) {
		try {
			int width = 200;
			int height = 200;

			BitMatrix matrix = new MultiFormatWriter()
				.encode(qrText, BarcodeFormat.QR_CODE, width, height);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			MatrixToImageWriter.writeToStream(matrix, "PNG", out);

			return out.toByteArray();

		} catch (WriterException e) {
			throw new RuntimeException("QR 코드 생성 실패", e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
