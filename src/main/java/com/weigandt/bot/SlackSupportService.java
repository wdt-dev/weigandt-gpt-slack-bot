package com.weigandt.bot;

import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.conversations.ConversationsHistoryRequest;
import com.slack.api.methods.response.conversations.ConversationsHistoryResponse;
import com.slack.api.model.Message;
import com.slack.api.model.event.MessageEvent;
import lombok.Getter;
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

    public boolean isValidToAnswerMsg(String input, String user) {
        String userMentioned = String.format("<@%s>", user);
        return BOT_ID.equals(user) || input.contains(userMentioned) || input.contains(BOT_MENTIONED);
    }

}
