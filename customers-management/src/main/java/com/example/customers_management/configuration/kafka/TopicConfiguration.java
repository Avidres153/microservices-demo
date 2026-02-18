package com.example.kafka_project.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class AutoConfigure {

    @Value("${spring.kafka.topic}")
    private String topicName;

    @Bean
    public NewTopic createNewTopic(){
        return TopicBuilder
                .name(topicName)
                .partitions(3)
                .replicas(1) // numero de replicas que se van a crear para las particiones
                .build();
    }
}
