package com.weigandt.chatsettings.repository;

import com.weigandt.chatsettings.entity.UserSetting;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

@Profile("tokens-restriction")
public interface UserSettingRepository extends MongoRepository<UserSetting, String> {
}
