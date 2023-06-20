package com.weigandt.chatsettings;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("tokens-restriction")
@Configuration
@EnableAutoConfiguration
public class SpringDataEnable {
}
