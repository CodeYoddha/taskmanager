package com.taskmanager.dto;

import com.taskmanager.entity.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Broadcast to /topic/project/{projectId} whenever a task changes,
 * so every connected client viewing that project's board updates instantly.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskEvent {
    private String eventType; // "CREATED", "STATUS_CHANGED", "DELETED"
    private Long taskId;
    private String title;
    private TaskStatus status;
    private Long projectId;
    private String assigneeName;
}
