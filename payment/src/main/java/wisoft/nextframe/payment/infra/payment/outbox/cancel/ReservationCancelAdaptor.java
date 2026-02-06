package wisoft.nextframe.payment.infra.payment.outbox.cancel;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.payment.application.payment.port.output.ReservationCancelClient;
import wisoft.nextframe.payment.application.payment.outbox.cancel.ReservationCancelExternalCallFailedException;
import wisoft.nextframe.payment.application.payment.outbox.cancel.ReservationCancelTemporarilyUnavailableException;
import wisoft.nextframe.payment.domain.ReservationId;

/**
 * 공연 서비스(Performance 서버)와 통신하여 예약을 취소하는 어댑터 구현체.
 * - `ReservationCancelClient` 포트를 구현하여 결제 실패 시 예약 취소 기능을 제공합니다.
 * - DELETE /reservations/{reservation-id} API를 호출합니다.
 * - 이미 취소된 예약에 대해서는 멱등하게 처리됩니다 (200 OK).
 */
@Slf4j
@Component
public class ReservationCancelAdaptor implements ReservationCancelClient {

	private final RestClient restClient;

	public ReservationCancelAdaptor(
		RestClient.Builder builder,
		@Value("${srt-service.url}") String baseUrl) {
		var factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(1_000); // 연결 시도 최대 1초
		factory.setReadTimeout(2_000);    // 응답 대기 최대 2초

		this.restClient = builder
			.baseUrl(baseUrl)
			.requestFactory(factory)
			.build();
	}

	@Override
	@CircuitBreaker(name = "reservationCancel", fallbackMethod = "cancelReservationFallback")
	public void cancelReservation(ReservationId reservationId) {
		log.debug("예약 취소 요청 - reservationId={}", reservationId.value());

		restClient.delete()
			.uri("/reservations/{reservationId}", reservationId.value())
			.retrieve()
			.toBodilessEntity();

		log.info("예약 취소 완료 - reservationId={}", reservationId.value());
	}

	private void cancelReservationFallback(ReservationId reservationId, Throwable e) {
		if (e instanceof CallNotPermittedException) {
			log.warn("예약 취소 차단됨 [CIRCUIT_BREAKER_OPEN]. reservationId={}", reservationId.value());
			throw new ReservationCancelTemporarilyUnavailableException(reservationId, e);
		}

		log.warn("예약 취소 공연 서버 호출 실패 [RESERVATION_CANCEL_EXTERNAL_CALL_FAILED]. reservationId={}, error={}",
			reservationId.value(), e.toString());
		throw new ReservationCancelExternalCallFailedException(reservationId, e);
	}
}
