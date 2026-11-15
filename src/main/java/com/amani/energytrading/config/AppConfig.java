package com.amani.energytrading.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.time.Clock;

@Configuration
public class AppConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public NewTopic tradesBookedTopic(@Value("${app.kafka.topics.trades-booked}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }
}
