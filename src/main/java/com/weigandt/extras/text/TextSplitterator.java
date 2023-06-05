package com.weigandt.extras.text;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@Slf4j
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Setter
@Getter
@Profile("create-embeddings")
public class TextSplitterator {
    private Integer chunkSize;
    private Integer overlap;
    private final Pattern pattern = Pattern.compile("[^a-zA-Z0-9 ]");
    private static final List<String> symbolsToClean = Arrays.asList(".", "[", "]", "(", ")", "■", "®", "©", "TM");

    public List<String> split(String text) {
        String[] words = cleanup(text).split(" ");
        int ptr = 0;
        int wordCount = 0;
        StringBuilder sb = new StringBuilder();
        List<String> chunks = new ArrayList<>();
        while (ptr < words.length) {
            String nextWord = words[ptr];
            if (isNotBlank(nextWord)) {
                sb.append(nextWord).append(" ");
            }
            wordCount++;
            if (wordCount == chunkSize) {
                String chunk = StringUtils.trim(sb.toString());
                if (isNotBlank(chunk)) {
                    chunks.add(chunk);
                    log.debug("Add chunk: {}", StringUtils.substring(chunk, 0, 50));
                    sb = new StringBuilder();
                }
                ptr -= overlap;
                wordCount = 0;
            }
            ptr++;
        }
        if (StringUtils.isNotBlank(sb)) {
            chunks.add(sb.toString());
        }
        log.debug("Add chunks: {}", chunks.size());
        return chunks;
    }

    private String cleanup(String src) {
        String result = pattern.matcher(src).replaceAll(SPACE);
        for (String s : symbolsToClean) {
            result = result.replace(s, SPACE);
        }

        return StringUtils.trim(result);
    }

}
