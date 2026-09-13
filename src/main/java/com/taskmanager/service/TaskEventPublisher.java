package com.taskmanager.service;

import com.taskmanager.config.RabbitMQConfig;
import com.taskmanager.dto.TaskAssignedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishTaskAssigned(TaskAssignedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.TASK_EXCHANGE,
                RabbitMQConfig.TASK_ASSIGNED_ROUTING_KEY,
                event
        );
    }
}
