package com.weigandt.bot.dto;

import com.slack.api.methods.MethodsClient;

public record ContextDto(String botToken, MethodsClient client,
                         String botUserId, String channelId,
                         long softThresholdMs, long hardThresholdMs) {
}
