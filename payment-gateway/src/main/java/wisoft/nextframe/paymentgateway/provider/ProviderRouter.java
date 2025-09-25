package wisoft.nextframe.paymentgateway.provider;

import static wisoft.nextframe.paymentgateway.api.PaymentGatewayController.*;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class ProviderRouter {

	private final List<PaymentProvider> providers;

	public ProviderRouter(List<PaymentProvider> providers) {
		this.providers = providers;
	}

	public ConfirmResponse confirm(String providerName, ConfirmRequest request) {
		return providers.stream()
			.filter(provider -> provider.supports(providerName))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("지원하지 않는 결제수단입니다."))
			.confirm(request);
	}
}
