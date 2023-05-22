package com.weigandt.extras.text;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
@Getter
@RequiredArgsConstructor
public class TextProcessorService {

    @Value("${file.processor.chunk.size}")
    private Integer chunkSize;
    @Value("${file.processor.overlap}")
    private Integer overlap;
    private final TextSplitterator textSplitterator;

    @PostConstruct
    private void postConstruct() {
        textSplitterator.setOverlap(getOverlap());
        textSplitterator.setChunkSize(getChunkSize());
    }

    public List<String> splitToChunks(String content) {
        return textSplitterator.split(content);
    }

}
