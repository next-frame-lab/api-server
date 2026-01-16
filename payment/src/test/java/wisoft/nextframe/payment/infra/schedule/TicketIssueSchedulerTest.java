package wisoft.nextframe.payment.infra.schedule;

import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import wisoft.nextframe.payment.application.ticketissue.TicketIssueRetryUseCase;

@ExtendWith(MockitoExtension.class)
class TicketIssueSchedulerTest {

    @Mock
    TicketIssueRetryUseCase retryUseCase;

    @InjectMocks
    TicketIssueScheduler scheduler;

    @Test
    @DisplayName("PENDING이 0이면 runOnce()를 호출하지 않는다.")
    void whenNoPending_doesNotRunOnce() {
        // given
        given(retryUseCase.hasPending()).willReturn(false);

        // when
        scheduler.retryTicketIssue();

        // then
        then(retryUseCase).should(never()).runOnce();
    }

    @Test
    @DisplayName("PENDING 있으면 runOnce() 호출된다")
    void whenPendingExists_runsOnce() {
        // given
        given(retryUseCase.hasPending()).willReturn(true);

        // when
        scheduler.retryTicketIssue();

        // then
        then(retryUseCase).should(times(1)).runOnce();
    }
}
