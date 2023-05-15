package com.weigandt.extras.text;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Getter
@RequiredArgsConstructor
public class TextProcessorService {

    @Value("${file.processor.chunk.size}")
    private Integer chunkSize;
    @Value("${file.processor.overlap}")
    private Integer overlap;

    private final ApplicationContext applicationContext;

    private TextSplitterator getTextSplitterator() {
        TextSplitterator splitterator = applicationContext.getBean(TextSplitterator.class);
        splitterator.setOverlap(getOverlap());
        splitterator.setChunkSize(getChunkSize());
        return splitterator;
    }

    public List<String> splitToChunks(String content) {
        return getTextSplitterator().split(content);
    }

}
