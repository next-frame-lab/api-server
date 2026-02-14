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
		return findProvider(providerName).confirm(request);
	}

	public CancelResponse cancel(String providerName, CancelRequest request) {
		return findProvider(providerName).cancel(request);
	}

	private PaymentProvider findProvider(String providerName) {
		return providers.stream()
			.filter(provider -> provider.supports(providerName))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("지원하지 않는 결제수단입니다."));
	}
}
