package com.weigandt.chatsettings.repository;

import com.weigandt.chatsettings.entity.DateAndUser;
import com.weigandt.chatsettings.entity.TokenUsageStatistic;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

@Profile("tokens-restriction")
public interface TokenUsageStatisticRepository extends MongoRepository<TokenUsageStatistic, DateAndUser> {
}
