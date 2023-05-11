package com.weigandt.bot;

import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.conversations.ConversationsHistoryRequest;
import com.slack.api.methods.response.conversations.ConversationsHistoryResponse;
import com.slack.api.model.Message;
import com.slack.api.model.event.MessageEvent;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

import static com.weigandt.Constants.SLACK_BOT.BOT_ID;

@Service
@Getter
public class SlackSupportService {
    private static final String BOT_MENTIONED = String.format("<@%s>", BOT_ID);

    public List<Message> getMsgHistory(MessageEvent event, EventContext ctx) throws SlackApiException, IOException {
        ConversationsHistoryRequest historyRequest = ConversationsHistoryRequest.builder()
                .channel(event.getChannel())
                //.token(BOT_TOKEN)
                .token(ctx.getBotToken())
                .build();
        ConversationsHistoryResponse history = ctx.client().conversationsHistory(historyRequest);
        return history.getMessages();
    }

    public boolean isNotValidToAnswerMsg(MessageEvent event) {
        String input = event.getText();
        String user = event.getUser();

        String userMentioned = String.format("<@%s>", user);//|| input.contains(BOT_MENTIONED)
        return BOT_ID.equals(user) || input.contains(userMentioned);
    }

    public boolean isValidToAnswerMsg(MessageEvent event) {
        String input = event.getText();
        return input.contains(BOT_MENTIONED);
    }

    public String cleanupMessage(String input) {
        if (input.contains("@") || input.contains("<") || input.contains(">")) {
            return StringUtils.trim(input.replaceAll("<@.*?>", ""));
        }
        return input;
    }
}
