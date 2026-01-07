package wisoft.nextframe.schedulereservationticketing.service.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.never;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.common.lock.DistributedLockManager;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.request.ReservationRequest;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.response.ReservationResponse;
import wisoft.nextframe.schedulereservationticketing.service.seat.SeatStateService;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private ReservationExecutor reservationExecutor;

    @Mock
    private DistributedLockManager distributedLockManager;

    @Mock
    private SeatStateService seatStateService;

    @Test
    @DisplayName("정상적인 예매 요청 시 DistributedLockManager를 통해 Executor를 호출한다")
    void reserveSeat_success() {
        // given
        UUID userId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        UUID performanceId = UUID.randomUUID();
        UUID seatId = UUID.randomUUID();
        int totalAmount = 10000;

        ReservationRequest request = new ReservationRequest(performanceId, scheduleId, List.of(seatId), 0L, totalAmount);

        ReservationResponse expectedResponse = ReservationResponse.builder()
            .reservationId(UUID.randomUUID())
            .totalAmount(totalAmount)
            .build();

        // DistributedLockManager가 businessLogic을 실행하고 결과를 반환하도록 설정
        given(distributedLockManager.executeWithLock(any(), any(Supplier.class)))
            .willAnswer(invocation -> {
                Supplier<ReservationResponse> businessLogic = invocation.getArgument(1);
                return businessLogic.get(); // 전달된 로직(람다)을 그대로 실행
            });

        // Executor가 정상 응답을 반환하도록 설정
        given(reservationExecutor.reserve(userId, scheduleId, performanceId, List.of(seatId), totalAmount))
            .willReturn(expectedResponse);

        // when
        ReservationResponse response = reservationService.reserveSeat(userId, request);

        // then
        assertThat(response).isEqualTo(expectedResponse);
        verify(distributedLockManager).executeWithLock(any(), any());
        verify(reservationExecutor).reserve(userId, scheduleId, performanceId, List.of(seatId), totalAmount);
        verify(seatStateService).evictSeatStatesCache(scheduleId);
    }

    @Test
    @DisplayName("락 획득에 실패하면(예외 발생) Executor는 호출되지 않는다")
    void reserveSeat_fail_lock_acquisition() {
        // given
        UUID userId = UUID.randomUUID();
        ReservationRequest request = new ReservationRequest(
            UUID.randomUUID(), UUID.randomUUID(), List.of(UUID.randomUUID()), 0L, 10000
        );

        // DistributedLockManager가 락 획득 실패 예외를 던지도록 설정
        given(distributedLockManager.executeWithLock(any(), any(Supplier.class)))
            .willThrow(new DomainException(ErrorCode.SEAT_ALREADY_LOCKED));

        // when and then
        assertThatThrownBy(() -> reservationService.reserveSeat(userId, request))
            .isInstanceOf(DomainException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.SEAT_ALREADY_LOCKED);

        // Executor는 절대 호출되면 안 됨
        verify(reservationExecutor, never()).reserve(any(), any(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("데드락 방지를 위해 좌석 ID 오름차순으로 정렬된 락 키를 요청한다")
    void reserveSeat_verify_lock_key_order() {
        // given
        UUID scheduleId = UUID.randomUUID();
        UUID seat1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID seat2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

        // 요청은 역순(seat2, seat1)으로 들어옴
        ReservationRequest request = new ReservationRequest(
            UUID.randomUUID(), scheduleId, List.of(seat2, seat1), 0L, 10000
        );

        // when
        reservationService.reserveSeat(UUID.randomUUID(), request);

        // then
        ArgumentCaptor<List<String>> lockKeysCaptor = ArgumentCaptor.forClass(List.class);
        verify(distributedLockManager).executeWithLock(lockKeysCaptor.capture(), any());

        List<String> capturedKeys = lockKeysCaptor.getValue();
        assertThat(capturedKeys.get(0)).contains(seat1.toString());
        assertThat(capturedKeys.get(1)).contains(seat2.toString());
        verify(seatStateService).evictSeatStatesCache(scheduleId);
    }

    @Test
    @DisplayName("Executor 실행 중 예외가 발생하면 해당 예외가 그대로 전파된다")
    void reserveSeat_fail_business_exception_from_executor() {
        // given
        UUID userId = UUID.randomUUID();
        ReservationRequest request = new ReservationRequest(
            UUID.randomUUID(), UUID.randomUUID(), List.of(UUID.randomUUID()), 0L, 10000
        );

        // DistributedLockManager가 businessLogic을 실행하도록 설정
        given(distributedLockManager.executeWithLock(any(), any(Supplier.class)))
            .willAnswer(invocation -> {
                Supplier<ReservationResponse> businessLogic = invocation.getArgument(1);
                return businessLogic.get();
            });

        // Executor가 예외를 던지도록 설정
        given(reservationExecutor.reserve(any(), any(), any(), any(), anyInt()))
            .willThrow(new DomainException(ErrorCode.TOTAL_PRICE_MISMATCH));

        // when and then
        assertThatThrownBy(() -> reservationService.reserveSeat(userId, request))
            .isInstanceOf(DomainException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.TOTAL_PRICE_MISMATCH);

        verify(reservationExecutor).reserve(any(), any(), any(), any(), anyInt());
    }
}
