package com.weigandt.chatsettings.entity;

import lombok.Data;
import org.springframework.context.annotation.Profile;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Profile("tokens-restriction")
public class DateAndUser implements Serializable {
    private String userName;
    private LocalDate accessDate;
}
