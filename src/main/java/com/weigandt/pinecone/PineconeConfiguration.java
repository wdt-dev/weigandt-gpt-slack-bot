package com.weigandt.pinecone;

import io.pinecone.PineconeClient;
import io.pinecone.PineconeClientConfig;
import io.pinecone.PineconeConnection;
import io.pinecone.PineconeConnectionConfig;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class PineconeConfiguration {

    @Value("${pinecone.apikey}")
    private String apiKey;
    @Value("${pinecone.environment}")
    private String env;
    @Value("${pinecone.project.name}")
    private String projectName;
    @Value("${pinecone.index.name}")
    private String indexName;

    @Bean
    public PineconeConnection getPineconeConnection() {
        PineconeClientConfig configuration = new PineconeClientConfig()
                .withApiKey(getApiKey())
                .withEnvironment(getEnv())
                .withProjectName(getProjectName());

        PineconeClient pineconeClient = new PineconeClient(configuration);

        PineconeConnectionConfig connectionConfig = new PineconeConnectionConfig()
                .withIndexName(getIndexName());

        return pineconeClient.connect(connectionConfig);
    }
}
