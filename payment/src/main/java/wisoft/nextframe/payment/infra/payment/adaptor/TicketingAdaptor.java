package wisoft.nextframe.payment.infra.payment.adaptor;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import wisoft.nextframe.payment.application.payment.port.output.TicketingClient;
import wisoft.nextframe.payment.domain.ReservationId;

@Component
public class TicketingAdaptor implements TicketingClient {

	private final RestClient restClient;

	public TicketingAdaptor(RestClient.Builder builder) {
		this.restClient = builder
			.baseUrl("http://localhost:8081/api/v1")
			.build();
	}

	@Override
	public void issueTicket(ReservationId reservationId) {
		restClient.post()
			.uri("/issue")
			.body(reservationId)
			.retrieve()
			.toBodilessEntity();
	}
}
