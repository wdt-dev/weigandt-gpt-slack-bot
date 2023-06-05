package com.weigandt.chatsettings.repository;

import com.weigandt.chatsettings.entity.DateAndUser;
import com.weigandt.chatsettings.entity.TokenUsageStatistic;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

@Profile("tokens-restriction")
public interface TokenUsageStatisticRepository extends JpaRepository<TokenUsageStatistic, DateAndUser> {
}
