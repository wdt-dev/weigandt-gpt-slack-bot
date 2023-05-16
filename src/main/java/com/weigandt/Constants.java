package com.weigandt;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SLACK_BOT {
        public static final String CMD_HELLO = "/hello";
        public static final String CMD_EXTRAS = "/extras";
        public static final String CMD_IMPORT = "/import";
        public static final String LOG_EXT = ".log";
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class OPENAI {
        public static final String VARIOUS_TEXT = "variousText";
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class PINECONE {
        public static final String F_SOURCE_TEXT = "sourceText";
        public static final String F_FILE_NAME = "fileName";

    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FILE_PROCESSOR {
        public static final String PDF_EXT = ".pdf";
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SEARCH {
        public static final String QA_PROMPT = """
                        You are an AI assistant providing helpful advice. You are given the following extracted parts of a long document and a question. Provide a conversational answer based on the context provided.
                        You should only provide hyperlinks that reference the context below. Do NOT make up hyperlinks.
                        If you can't find the answer in the context below, just say "Hmm, I'm not sure." Don't try to make up an answer.
                        If the question is not related to the context, politely respond that you are tuned to only answer questions that are related to the context.

                        Question: {question}
                =========
                        {context}
                =========
                        Answer in Markdown:""";

        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static class PARAMS {
            public static final String CHAT_HISTORY = "{chat_history}";
            public static final String CONTEXT = "{context}";
            public static final String QUESTION = "{question}";
        }

        public static final String REPHRASE_PROMPT = """
                Given the following conversation and a follow up question, rephrase the follow up question to be a standalone question.

                Chat History:
                {chat_history}
                Follow Up Input: {question}
                Standalone question:""";

    }
}