package wisoft.nextframe.payment.infra.payment.adaptor;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import wisoft.nextframe.payment.application.payment.port.output.PaymentGateway;

@Component
@Profile({"loadtest","dev"})
public class StubPaymentGatewayAdaptor implements PaymentGateway {

    @Override
    public PaymentConfirmResult confirmPayment(String paymentKey, String orderId, int amount) {
        // 테스트용 : 항상 성공하는 응답 반환
        return new PaymentConfirmResult(true, amount, null, null);
    }
}
