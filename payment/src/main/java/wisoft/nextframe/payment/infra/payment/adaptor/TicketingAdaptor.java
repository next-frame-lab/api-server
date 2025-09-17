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
			.baseUrl("http://127.0.0.1:18081/api/v1")
			.build();
	}

	@Override
	public void issueTicket(ReservationId reservationId) {
		TicketIssueRequest request = new TicketIssueRequest(reservationId.getValue());

		restClient.post()
			.uri("/tickets")
			.body(request)
			.retrieve()
			.toBodilessEntity();
	}
}
