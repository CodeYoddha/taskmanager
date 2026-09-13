package com.taskmanager.service;

import com.taskmanager.config.RabbitMQConfig;
import com.taskmanager.dto.TaskAssignedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TaskAssignedListener {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.TASK_ASSIGNED_QUEUE)
    public void handleTaskAssigned(TaskAssignedEvent event) {
        log.info("Consuming task-assigned event for {}", event.getAssigneeEmail());
        emailService.sendTaskAssignedEmail(
                event.getAssigneeEmail(),
                event.getTaskTitle(),
                event.getProjectName()
        );
    }
}
