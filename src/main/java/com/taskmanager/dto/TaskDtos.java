package com.taskmanager.dto;

import com.taskmanager.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

public class TaskDtos {

    @Data
    public static class CreateTaskRequest {
        @NotBlank
        private String title;
        private String description;
        @NotNull
        private Long projectId;
        private Long assigneeId;
        private Instant dueDate;
    }

    @Data
    public static class UpdateTaskStatusRequest {
        @NotNull
        private TaskStatus status;
    }

    @Data
    public static class TaskResponse {
        private Long id;
        private String title;
        private String description;
        private TaskStatus status;
        private Long projectId;
        private String assigneeName;
        private String createdByName;
        private Instant dueDate;
        private Instant updatedAt;
    }
}
