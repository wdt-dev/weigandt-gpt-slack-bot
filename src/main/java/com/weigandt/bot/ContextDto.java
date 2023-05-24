package com.weigandt.bot;

import com.slack.api.methods.MethodsClient;
import org.slf4j.Logger;

public record ContextDto(String botToken, MethodsClient client, Logger logger, String botUserId, String channelId) {
}
