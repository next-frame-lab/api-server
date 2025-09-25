package wisoft.nextframe.paymentgateway.provider;

import static wisoft.nextframe.paymentgateway.api.PaymentGatewayController.*;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"loadtest", "dev"})
public class StubPaymentProvider implements PaymentProvider {

	@Override
	public boolean supports(String providerName) {
		return "toss".equalsIgnoreCase(providerName);
	}

	@Override
	public ConfirmResponse confirm(ConfirmRequest request) {
		return new ConfirmResponse(true, request.amount(), null, null);
	}
}