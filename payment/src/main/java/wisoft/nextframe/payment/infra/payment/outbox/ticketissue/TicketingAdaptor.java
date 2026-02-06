package wisoft.nextframe.payment.infra.payment.outbox.ticketissue;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.payment.application.payment.outbox.ticketissue.TicketIssueExternalCallFailedException;
import wisoft.nextframe.payment.application.payment.outbox.ticketissue.TicketIssueInvalidResponseException;
import wisoft.nextframe.payment.application.payment.outbox.ticketissue.TicketIssueResult;
import wisoft.nextframe.payment.application.payment.outbox.ticketissue.TicketIssueTemporarilyUnavailableException;
import wisoft.nextframe.payment.application.payment.port.output.TicketingClient;
import wisoft.nextframe.payment.domain.ReservationId;
import wisoft.nextframe.payment.infra.payment.outbox.ticketissue.dto.TicketIssueRequest;
import wisoft.nextframe.payment.infra.payment.outbox.ticketissue.dto.TicketIssueResponse;

/**
 * Ticketing 외부 서비스(SRT 서버)와 통신하는 어댑터 구현체.
 */
@Slf4j
@Component
public class TicketingAdaptor implements TicketingClient {

	private final RestClient restClient;

	public TicketingAdaptor(RestClient.Builder builder, @Value("${srt-service.url}") String baseUrl) {
		var factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(1_000);
		factory.setReadTimeout(2_000);

		this.restClient = builder
			.baseUrl(baseUrl)
			.requestFactory(factory)
			.build();
	}

	@Override
	@CircuitBreaker(name = "ticketing", fallbackMethod = "issueTicketFallback")
	public TicketIssueResult issueTicket(ReservationId reservationId) {
		TicketIssueResponse response = restClient.post()
			.uri("/tickets")
			.contentType(MediaType.APPLICATION_JSON)
			.body(new TicketIssueRequest(reservationId.value()))
			.retrieve()
			.body(TicketIssueResponse.class);

		if (response == null || response.ticketId() == null) {
			throw new TicketIssueInvalidResponseException(reservationId);
		}

		return new TicketIssueResult(response.ticketId());
	}

	private TicketIssueResult issueTicketFallback(ReservationId reservationId, Throwable e) {
		if (e instanceof CallNotPermittedException) {
			log.warn("티켓 발급 차단됨 [CIRCUIT_BREAKER_OPEN]. reservationId={}", reservationId.value());
			throw new TicketIssueTemporarilyUnavailableException(reservationId, e);
		}

		log.warn("티켓 발급 외부 호출 실패 [TICKET_ISSUE_EXTERNAL_CALL_FAILED]. reservationId={}, error={}",
			reservationId.value(), e.toString());
		throw new TicketIssueExternalCallFailedException(reservationId, e);
	}
}
