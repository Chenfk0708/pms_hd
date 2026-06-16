package com.jeez.zp.room.security;

import com.jeez.zp.room.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ChannelCallbackAuthVerifier {

    public static final String TEST_TOKEN_HEADER = "X-Channel-Test-Token";
    public static final String OPERATOR_ID_HEADER = "X-Channel-Operator-Id";
    private static final String AUTH_FAILED_MESSAGE = "第三方渠道回调认证失败";

    @Value("${jeez.channel.callback.test-token:}")
    private String channelCallbackTestToken;

    public Long verifyAndGetOperatorId(String testToken, String operatorId) {
        verifyTestToken(testToken);
        return parseOperatorUserId(operatorId);
    }

    private void verifyTestToken(String testToken) {
        if (channelCallbackTestToken == null
                || channelCallbackTestToken.isBlank()
                || testToken == null
                || !channelCallbackTestToken.equals(testToken)) {
            throw new BusinessException(401, AUTH_FAILED_MESSAGE);
        }
    }

    private Long parseOperatorUserId(String operatorId) {
        if (operatorId == null || operatorId.isBlank()) {
            throw new BusinessException(40001, "缺少第三方渠道操作人: " + OPERATOR_ID_HEADER);
        }
        try {
            return Long.valueOf(operatorId);
        } catch (NumberFormatException exception) {
            throw new BusinessException(40001, "第三方渠道操作人格式不正确: " + OPERATOR_ID_HEADER);
        }
    }
}
