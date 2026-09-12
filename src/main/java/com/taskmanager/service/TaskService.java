package com.taskmanager.service;

import com.taskmanager.dto.TaskDtos.CreateTaskRequest;
import com.taskmanager.dto.TaskDtos.TaskResponse;
import com.taskmanager.dto.TaskEvent;
import com.taskmanager.entity.Project;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.entity.User;
import com.taskmanager.exception.ApiException;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectService projectService;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailService emailService;

    public Task createTask(CreateTaskRequest request, String creatorEmail) {
        Project project = projectService.getProjectOrThrow(request.getProjectId());
        User creator = getUserByEmail(creatorEmail);

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Assignee not found"));
        }

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .project(project)
                .assignee(assignee)
                .createdBy(creator)
                .dueDate(request.getDueDate())
                .status(TaskStatus.TODO)
                .build();

        Task saved = taskRepository.save(task);

        broadcastEvent(saved, "CREATED");

        if (assignee != null) {
            emailService.sendTaskAssignedEmail(assignee.getEmail(), saved.getTitle(), project.getName());
        }

        return saved;
    }

    public Page<Task> getTasksForProject(Long projectId, TaskStatus status, Pageable pageable) {
        if (status != null) {
            return taskRepository.findByProjectIdAndStatus(projectId, status, pageable);
        }
        return taskRepository.findByProjectId(projectId, pageable);
    }

    public Task updateStatus(Long taskId, TaskStatus newStatus) {
        Task task = getTaskOrThrow(taskId);
        task.setStatus(newStatus);
        Task saved = taskRepository.save(task);

        broadcastEvent(saved, "STATUS_CHANGED");

        return saved;
    }

    private void broadcastEvent(Task task, String eventType) {
        TaskEvent event = TaskEvent.builder()
                .eventType(eventType)
                .taskId(task.getId())
                .title(task.getTitle())
                .status(task.getStatus())
                .projectId(task.getProject().getId())
                .assigneeName(task.getAssignee() != null ? task.getAssignee().getFullName() : null)
                .build();

        // Every client subscribed to this project's topic gets this instantly
        messagingTemplate.convertAndSend("/topic/project/" + task.getProject().getId(), event);
    }

    public Task getTaskOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    public void deleteTask(Long id) {
        Task task = getTaskOrThrow(id);
        broadcastEvent(task, "DELETED");
        taskRepository.deleteById(id);
    }

    public TaskResponse toResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus());
        response.setProjectId(task.getProject().getId());
        response.setAssigneeName(task.getAssignee() != null ? task.getAssignee().getFullName() : null);
        response.setCreatedByName(task.getCreatedBy().getFullName());
        response.setDueDate(task.getDueDate());
        response.setUpdatedAt(task.getUpdatedAt());
        return response;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
