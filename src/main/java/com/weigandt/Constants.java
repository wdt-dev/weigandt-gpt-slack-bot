package com.weigandt;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SLACK_BOT {
        public static final String CMD_HELLO = "/hello";
        public static final String LOG_EXT = ".log";
        public static final String CHANNEL = "channel";
        public static final String IM = "im";
        public static final String CHAT_POST_MESSAGE_FAILED = "chat.postMessage failed: {}";
        public static final String NO_CHAT_ACCESS = "The bot can't get channel info for channel id:{}, pls check bot access";
        public static final String TOKEN_LIMIT_EXCEEDED = "Your day limit exceeded, welcome back another day. Symbols spent: %s";
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class OPENAI {
        public static final String FEATURE_NOT_ENABLED_MSG = "Feature not enabled, pls check your profiles";
        public static final String ERROR_ANSWERING_QUESTION = "Error answering question: {}";
        public static final Integer DEFAULT_SOFT_RESTRICTION = 50000;
        public static final Integer DEFAULT_HARD_RESTRICTION = 100000;
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SEARCH {
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static class PARAMS {
            public static final String CHAT_HISTORY = "{chat_history}";
            public static final String QUESTION = "{question}";
        }

        public static final String QA_WITH_HISTORY_PROMPT = """
                Given the following conversation of user and assistant, and a new question, please answer to the new question of user:

                Chat History:
                {chat_history}
                New question: {question}
                """;
        public static final String LIMITS_MSG = "Don't forget about limits:";
        public static final String BOT_IS_TYPING = "Bot is typing...";
        public static final String THE_ANSWER_IS_HUGE_MSG = "The answer is huge, please wait";
        public static final String CANT_ANSWER_MSG = "I tried to answer but I can't, please try again";
        public static final String HAS_JOINED_MSG = "has joined the channel";
    }
}