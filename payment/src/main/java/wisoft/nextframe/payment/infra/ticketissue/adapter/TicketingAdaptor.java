package wisoft.nextframe.payment.infra.ticketissue.adapter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.payment.application.payment.port.output.TicketingClient;
import wisoft.nextframe.payment.application.ticketissue.dto.TicketIssueResult;
import wisoft.nextframe.payment.application.ticketissue.exception.TicketIssueExternalCallFailedException;
import wisoft.nextframe.payment.application.ticketissue.exception.TicketIssueInvalidResponseException;
import wisoft.nextframe.payment.application.ticketissue.exception.TicketIssueTemporarilyUnavailableException;
import wisoft.nextframe.payment.domain.ReservationId;
import wisoft.nextframe.payment.infra.ticketissue.adapter.dto.TicketIssueRequest;
import wisoft.nextframe.payment.infra.ticketissue.adapter.dto.TicketIssueResponse;

/**
 * Ticketing 외부 서비스(SRT 서버)와 통신하는 어댑터 구현체.
 * - `TicketingClient` 포트를 구현하여 도메인 계층에서 티켓 발급 기능을 호출할 수 있도록 연결한다.
 * - 도메인의 `ReservationId`를 `TicketIssueRequest` DTO로 변환해 REST API로 전송하고, 응답을 `TicketIssueResponse`로 매핑하여 반환한다.
 * - 외부 서비스의 기본 URL은 `srt-service.url` 프로퍼티로 주입받는다.
 **/
@Slf4j
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
	@CircuitBreaker(name = "ticketing", fallbackMethod = "issueTicketFallback")
	public TicketIssueResult issueTicket(ReservationId reservationId) {
		TicketIssueResponse response = restClient.post()
			.uri("/tickets")
			.contentType(MediaType.APPLICATION_JSON)
			.body(new TicketIssueRequest(reservationId.value()))
			.retrieve()
			.body(TicketIssueResponse.class);

		if (response == null || response.ticketId() == null) {
			// 이 케이스도 외부 계약 위반이라서 adapter 레벨에서 의미 있는 예외로 변환
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