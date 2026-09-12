package com.taskmanager.controller;

import com.taskmanager.dto.TaskDtos.CreateTaskRequest;
import com.taskmanager.dto.TaskDtos.TaskResponse;
import com.taskmanager.dto.TaskDtos.UpdateTaskStatusRequest;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request, Authentication auth) {
        Task task = taskService.createTask(request, auth.getName());
        return ResponseEntity.ok(taskService.toResponse(task));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<Page<TaskResponse>> getByProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) TaskStatus status,
            Pageable pageable
    ) {
        Page<Task> tasks = taskService.getTasksForProject(projectId, status, pageable);
        return ResponseEntity.ok(tasks.map(taskService::toResponse));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskStatusRequest request
    ) {
        Task task = taskService.updateStatus(id, request.getStatus());
        return ResponseEntity.ok(taskService.toResponse(task));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
