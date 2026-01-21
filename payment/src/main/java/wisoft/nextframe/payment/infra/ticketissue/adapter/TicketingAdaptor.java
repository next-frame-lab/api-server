package wisoft.nextframe.payment.infra.ticketissue.adapter;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import wisoft.nextframe.payment.application.payment.port.output.TicketingClient;
import wisoft.nextframe.payment.application.ticketissue.dto.TicketIssueResult;
import wisoft.nextframe.payment.domain.ReservationId;
import wisoft.nextframe.payment.infra.ticketissue.adapter.dto.TicketIssueRequest;
import wisoft.nextframe.payment.infra.ticketissue.adapter.dto.TicketIssueResponse;

/**
 * Ticketing 외부 서비스(SRT 서버)와 통신하는 어댑터 구현체.
 * - `TicketingClient` 포트를 구현하여 도메인 계층에서 티켓 발급 기능을 호출할 수 있도록 연결한다.
 * - 도메인의 `ReservationId`를 `TicketIssueRequest` DTO로 변환해 REST API로 전송하고, 응답을 `TicketIssueResponse`로 매핑하여 반환한다.
 * - 외부 서비스의 기본 URL은 `srt-service.url` 프로퍼티로 주입받는다.
 **/
@Component
public class TicketingAdaptor implements TicketingClient {

	private final RestClient restClient;

	public TicketingAdaptor(RestClient.Builder builder, @Value("${srt-service.url}") String baseUrl) {
		var factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(1_000); // 연결 시도 최대 1초
		factory.setReadTimeout(2_000);    // 응답 대기 최대 2초

		this.restClient = builder
			.baseUrl(baseUrl)
			.requestFactory(factory)
			.build();
	}

	@Override
	@CircuitBreaker(name = "ticketing")
	public TicketIssueResult issueTicket(ReservationId reservationId) {
		TicketIssueResponse response = restClient.post()
			.uri("/tickets")
			.contentType(MediaType.APPLICATION_JSON)
			.body(new TicketIssueRequest(reservationId.value()))
			.retrieve()
			.body(TicketIssueResponse.class);

		return Optional.ofNullable(response)
			.map(res -> new TicketIssueResult(res.ticketId()))
			.orElseThrow(() -> new RuntimeException("티켓 발급 응답이 비어있습니다."));
	}
}