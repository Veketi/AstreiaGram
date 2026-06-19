package com.astreiagram.post_service.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PostEventPublisher {

    private static final String TOPIC = "post-created";

    private final KafkaTemplate<String, PostCreatedEvent> kafkaTemplate;

    public PostEventPublisher(KafkaTemplate<String, PostCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPostCreated(PostCreatedEvent event) {
        kafkaTemplate.send(TOPIC, event.postId(), event);
    }
}
