package wisoft.nextframe.payment.infra.payment.adaptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import wisoft.nextframe.payment.application.payment.port.output.TicketingClient;
import wisoft.nextframe.payment.domain.ReservationId;

@Component
public class TicketingAdaptor implements TicketingClient {

	private final RestClient restClient;

	public TicketingAdaptor(RestClient.Builder builder, @Value("${srt-service.url}") String baseUrl) {
		this.restClient = builder
			.baseUrl(baseUrl)
			.build();
	}

	@Override
	public void issueTicket(ReservationId reservationId) {
		TicketIssueRequest request = new TicketIssueRequest(reservationId.getValue());

		restClient.post()
			.uri("/tickets")
			.contentType(MediaType.APPLICATION_JSON)
			.body(request)
			.retrieve()
			.toBodilessEntity();
	}
}
