package wisoft.nextframe.payment.infra.payment.adaptor;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import wisoft.nextframe.payment.application.payment.port.output.PaymentGateway;

@SpringBootTest(classes = StubPaymentGatewayAdaptor.class)
@ActiveProfiles("loadtest") // StubPaymentGatewayAdaptor만 활성화
class StubPaymentGatewayAdaptorTest {

    @Autowired
    private PaymentGateway paymentGateway; // Stub이 주입됨

    @Test
    void stubAlwaysReturnsSuccess() {
        var result = paymentGateway.confirmPayment("dummy-key", "order-123", 10000);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.totalAmount()).isEqualTo(10000);
        assertThat(result.errorCode()).isNull();
        assertThat(result.errorMessage()).isNull();
    }
}
