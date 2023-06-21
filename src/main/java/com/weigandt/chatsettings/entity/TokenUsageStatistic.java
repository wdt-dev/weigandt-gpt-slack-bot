package com.weigandt.chatsettings.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("tokenusagestatictics")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Profile("tokens-restriction")
public class TokenUsageStatistic {
    @Id
    private DateAndUser id;
    private Integer tokenUsedQuestion;
    private Integer tokenUsedAnswer;
    private Integer tokenUsedTotal;
}
