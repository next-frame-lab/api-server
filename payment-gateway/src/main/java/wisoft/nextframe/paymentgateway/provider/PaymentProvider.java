package wisoft.nextframe.paymentgateway.provider;

import static wisoft.nextframe.paymentgateway.api.PaymentGatewayController.*;

public interface PaymentProvider {
	boolean supports(String providerName);

	ConfirmResponse confirm(ConfirmRequest request);
}
