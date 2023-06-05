package com.weigandt.chatsettings.repository;

import com.weigandt.chatsettings.entity.UserSetting;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

@Profile("tokens-restriction")
public interface UserSettingRepository extends JpaRepository<UserSetting, String> {
}
