package wisoft.nextframe.schedulereservationticketing.service.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.dto.auth.OAuthUserInfo;
import wisoft.nextframe.schedulereservationticketing.dto.auth.SigninResponse;

@ExtendWith(MockitoExtension.class)
class OAuthFacadeTest {

    @Mock
    private Map<String, OAuthProvider> providerMap;

    @Mock
    private OAuthSigninService signinService;

    @InjectMocks
    private OAuthFacade oAuthFacade;

    @Mock
    private OAuthProvider mockOAuthProvider;

    @Test
    @DisplayName("지원하는 프로바이더로 로그인 성공 테스트")
    void signin_with_supported_provider_should_succeed() {
        // given
        String providerName = "kakao";
        String authCode = "test_auth_code";
        OAuthUserInfo userInfo = new OAuthUserInfo(
          "provider",
          "providerUserId",
          "email",
          "name",
          "imageUrl"
        );
        SigninResponse expectedResponse = new SigninResponse(
          "access_token",
          "refresh_token",
          "imageUrl",
          "name",
          25,
          "email"
        );

        when(providerMap.get(providerName)).thenReturn(mockOAuthProvider);
        when(mockOAuthProvider.getUserInfo(authCode)).thenReturn(userInfo);
        when(signinService.processUserSignin(userInfo)).thenReturn(expectedResponse);

        // when
        SigninResponse actualResponse = oAuthFacade.signin(providerName, authCode);

        // then
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        verify(providerMap).get(providerName);
        verify(mockOAuthProvider).getUserInfo(authCode);
        verify(signinService).processUserSignin(userInfo);
    }

    @Test
    @DisplayName("지원하지 않는 프로바이더로 로그인 시 예외 발생 테스트")
    void signin_with_unsupported_provider_should_throw_exception() {
        // given
        String providerName = "unsupported_provider";
        String authCode = "test_auth_code";

        when(providerMap.get(providerName)).thenReturn(null);

        // when and then
        DomainException exception = assertThrows(DomainException.class, () -> {
            oAuthFacade.signin(providerName, authCode);
        });

        assertEquals(ErrorCode.UNSUPPORTED_PROVIDER, exception.getErrorCode());
        verify(providerMap).get(providerName);
        verify(mockOAuthProvider, never()).getUserInfo(anyString());
        verify(signinService, never()).processUserSignin(any());
    }
}
