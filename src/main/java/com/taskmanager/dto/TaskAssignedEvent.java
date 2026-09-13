package com.taskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Published to RabbitMQ instead of sending the email directly.
 * A separate listener consumes this and sends the actual email,
 * decoupling "a task was assigned" from "an email got sent".
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskAssignedEvent implements Serializable {
    private String assigneeEmail;
    private String taskTitle;
    private String projectName;
}
