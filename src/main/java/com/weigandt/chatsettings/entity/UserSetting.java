package com.weigandt.chatsettings.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document("usersettings")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Profile("tokens-restriction")
public class UserSetting {

    @Id
    private String userName;

    private LocalDate lastAccessDate;
    private Integer tokenSoftThreshold;
    private Integer tokenHardThreshold;
}
