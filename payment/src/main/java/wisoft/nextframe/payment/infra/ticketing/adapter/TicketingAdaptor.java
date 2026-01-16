package wisoft.nextframe.payment.infra.ticketing.adapter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import wisoft.nextframe.payment.application.payment.port.output.TicketingClient;
import wisoft.nextframe.payment.domain.ReservationId;
import wisoft.nextframe.payment.infra.ticketing.adapter.dto.TicketIssueRequest;
import wisoft.nextframe.payment.infra.ticketing.adapter.dto.TicketIssueResponse;

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
		this.restClient = builder
			.baseUrl(baseUrl)
			.build();
	}

	@Override
	public TicketIssueResponse issueTicket(ReservationId reservationId) {
		return restClient.post()
			.uri("/tickets")
			.contentType(MediaType.APPLICATION_JSON)
			.body(new TicketIssueRequest(reservationId.value()))
			.retrieve()
			.body(TicketIssueResponse.class);
	}
}
