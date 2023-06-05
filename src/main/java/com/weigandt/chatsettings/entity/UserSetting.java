package com.weigandt.chatsettings.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Profile;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "usersettings")
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
