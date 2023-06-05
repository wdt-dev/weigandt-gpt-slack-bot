package com.weigandt.chatsettings.service;

import com.weigandt.chatsettings.dto.TokenCountSettings;
import com.weigandt.chatsettings.dto.TokenUsageDto;
import com.weigandt.chatsettings.entity.TokenUsageStatistic;

import java.util.Optional;

public interface TokenUsageService {

    TokenCountSettings getUserTokenRestriction(String userName);
    boolean isSoftThresholdExceeded(String userName);
    boolean isHardThresholdExceeded(String userName);
    Optional<TokenUsageStatistic> getTodayStatistics(String userName);
    TokenUsageStatistic saveStatistics(TokenUsageDto dto);
}
