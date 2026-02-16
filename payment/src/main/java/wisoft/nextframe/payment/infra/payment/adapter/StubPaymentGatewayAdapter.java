package wisoft.nextframe.payment.infra.payment.adapter;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import wisoft.nextframe.payment.application.payment.port.output.PaymentGateway;

@Component
@Profile({"loadtest","dev"})
public class StubPaymentGatewayAdapter implements PaymentGateway {

    @Override
    public PaymentConfirmResult confirmPayment(String paymentKey, String orderId, int amount) {
        // 테스트용 : 항상 성공하는 응답 반환
        return new PaymentConfirmResult(true, amount, null, null);
    }

    @Override
    public PaymentCancelResult cancelPayment(String orderId, int cancelAmount, String cancelReason) {
        // 테스트용 : 항상 성공하는 응답 반환
        return new PaymentCancelResult(true, cancelAmount, "stub-tx-key", null, null);
    }
}
