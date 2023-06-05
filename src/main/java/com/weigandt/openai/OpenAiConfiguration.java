package com.weigandt.openai;

import com.theokanning.openai.service.OpenAiService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class OpenAiConfiguration {

    @Value("${openai.apikey:}")
    private String apiKey;

    @Bean
    public OpenAiService getOpenAiService() {
        return new OpenAiService(getApiKey());
    }
}
