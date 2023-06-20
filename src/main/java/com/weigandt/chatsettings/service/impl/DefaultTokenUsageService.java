package com.weigandt.chatsettings.service.impl;

import com.weigandt.chatsettings.dto.TokenCountSettingsDto;
import com.weigandt.chatsettings.dto.TokenUsageDto;
import com.weigandt.chatsettings.entity.DateAndUser;
import com.weigandt.chatsettings.entity.TokenUsageStatistic;
import com.weigandt.chatsettings.entity.UserSetting;
import com.weigandt.chatsettings.repository.TokenUsageStatisticRepository;
import com.weigandt.chatsettings.repository.UserSettingRepository;
import com.weigandt.chatsettings.service.TokenUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

import static com.weigandt.Constants.OPENAI.DEFAULT_HARD_RESTRICTION;
import static com.weigandt.Constants.OPENAI.DEFAULT_SOFT_RESTRICTION;
import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
@Profile("tokens-restriction")
public class DefaultTokenUsageService implements TokenUsageService {

    private final UserSettingRepository userSettingRepository;
    private final TokenUsageStatisticRepository tokenUsageStatisticRepository;

    @Override
    public TokenCountSettingsDto getUserTokenRestriction(String userName) {
        Optional<UserSetting> setting = userSettingRepository.findById(userName);
        if (setting.isEmpty()) {
            return new TokenCountSettingsDto(userName, DEFAULT_SOFT_RESTRICTION, DEFAULT_HARD_RESTRICTION);
        }
        return setting.map(this::toDto).get();
    }

    @Override
    public boolean isSoftThresholdExceeded(String userName) {
        TokenCountSettingsDto userTokenRestriction = getUserTokenRestriction(userName);
        Integer softThreshold = userTokenRestriction.softThreshold();
        Optional<TokenUsageStatistic> todayStatistics = getTodayStatistics(userName);
        return todayStatistics.filter(tus -> tus.getTokenUsedTotal() > softThreshold).isPresent();
    }

    @Override
    public boolean isHardThresholdExceeded(String userName) {
        TokenCountSettingsDto userTokenRestriction = getUserTokenRestriction(userName);
        Integer hardThreshold = userTokenRestriction.hardThreshold();
        Optional<TokenUsageStatistic> todayStatistics = getTodayStatistics(userName);
        return todayStatistics.filter(tus -> tus.getTokenUsedTotal() > hardThreshold).isPresent();
    }

    @Override
    public Optional<TokenUsageStatistic> getTodayStatistics(String userName) {
        LocalDate currentDate = LocalDate.now();
        DateAndUser key = new DateAndUser();
        key.setUserName(userName);
        key.setAccessDate(currentDate);
        return tokenUsageStatisticRepository.findById(key);
    }

    @Override
    public TokenUsageStatistic saveStatistics(TokenUsageDto dto) {
        LocalDate currentDate = LocalDate.now();
        DateAndUser key = new DateAndUser();
        key.setUserName(dto.userName());
        key.setAccessDate(currentDate);
        TokenUsageStatistic statisticRecord = tokenUsageStatisticRepository.findById(key)
                .orElseGet(TokenUsageStatistic::new);
        if (isNull(statisticRecord.getId())) {
            statisticRecord.setId(key);
        }
        Integer usageQuestions = calcUsage(dto.usageQuestions(), statisticRecord.getTokenUsedQuestion());
        Integer usageAnswers = calcUsage(dto.usageAnswer(), statisticRecord.getTokenUsedAnswer());
        statisticRecord.setTokenUsedQuestion(usageQuestions);
        statisticRecord.setTokenUsedAnswer(usageAnswers);
        statisticRecord.setTokenUsedTotal(usageQuestions + usageAnswers);
        return tokenUsageStatisticRepository.save(statisticRecord);
    }

    private static Integer calcUsage(Integer usage, Integer existingUsage) {
        return usage + Optional.ofNullable(existingUsage).orElse(0);
    }

    private TokenCountSettingsDto toDto(UserSetting model) {
        return new TokenCountSettingsDto(model.getUserName(),
                model.getTokenSoftThreshold(),
                model.getTokenHardThreshold());
    }
}
