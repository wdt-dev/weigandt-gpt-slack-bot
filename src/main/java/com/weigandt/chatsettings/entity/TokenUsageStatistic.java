package com.weigandt.chatsettings.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Profile;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tokenusagestatictics")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Profile("tokens-restriction")
public class TokenUsageStatistic {
    @EmbeddedId
    private DateAndUser id;
    private Integer tokenUsedQuestion;
    private Integer tokenUsedAnswer;
    private Integer tokenUsedTotal;
}
