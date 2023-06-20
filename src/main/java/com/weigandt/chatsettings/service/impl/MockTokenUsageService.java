package com.weigandt.chatsettings.service.impl;

import com.weigandt.chatsettings.dto.TokenCountSettingsDto;
import com.weigandt.chatsettings.dto.TokenUsageDto;
import com.weigandt.chatsettings.entity.TokenUsageStatistic;
import com.weigandt.chatsettings.service.TokenUsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.weigandt.Constants.OPENAI.DEFAULT_HARD_RESTRICTION;
import static com.weigandt.Constants.OPENAI.DEFAULT_SOFT_RESTRICTION;
import static com.weigandt.Constants.OPENAI.FEATURE_NOT_ENABLED_MSG;

@Service
@Slf4j
@RequiredArgsConstructor
@Profile("!tokens-restriction")
public class MockTokenUsageService implements TokenUsageService {

    @Override
    public TokenCountSettingsDto getUserTokenRestriction(String userName) {
        log.warn(FEATURE_NOT_ENABLED_MSG);
        return new TokenCountSettingsDto(userName, DEFAULT_SOFT_RESTRICTION, DEFAULT_HARD_RESTRICTION);
    }

    @Override
    public boolean isSoftThresholdExceeded(String userName) {
        return thresholdMockResult();
    }

    @Override
    public boolean isHardThresholdExceeded(String userName) {
        return thresholdMockResult();
    }

    @Override
    public Optional<TokenUsageStatistic> getTodayStatistics(String userName) {
        log.warn(FEATURE_NOT_ENABLED_MSG);
        return Optional.empty();
    }

    @Override
    public TokenUsageStatistic saveStatistics(TokenUsageDto dto) {
        log.warn(FEATURE_NOT_ENABLED_MSG);
        return null;
    }

    private static boolean thresholdMockResult() {
        log.warn(FEATURE_NOT_ENABLED_MSG);
        return false;
    }
}
