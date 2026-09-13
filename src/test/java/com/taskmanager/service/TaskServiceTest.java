package com.taskmanager.service;

import com.taskmanager.dto.TaskAssignedEvent;
import com.taskmanager.dto.TaskDtos.CreateTaskRequest;
import com.taskmanager.entity.Project;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.entity.User;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private TaskEventPublisher taskEventPublisher;

    @InjectMocks
    private TaskService taskService;

    private Project project;
    private User creator;

    @BeforeEach
    void setUp() {
        creator = User.builder().id(1L).email("creator@example.com").fullName("Creator").build();
        project = Project.builder().id(10L).name("Test Project").owner(creator).build();
    }

    @Test
    void createTask_withoutAssignee_doesNotPublishEvent() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Write tests");
        request.setProjectId(10L);

        when(projectService.getProjectOrThrow(10L)).thenReturn(project);
        when(userRepository.findByEmail("creator@example.com")).thenReturn(Optional.of(creator));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
            Task t = inv.getArgument(0);
            t.setId(100L);
            return t;
        });

        Task result = taskService.createTask(request, "creator@example.com");

        assertThat(result.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(result.getAssignee()).isNull();

        // No assignee means no email event should be published
        verify(taskEventPublisher, never()).publishTaskAssigned(any(TaskAssignedEvent.class));

        // But the WebSocket broadcast should still fire for anyone watching the board
        verify(messagingTemplate).convertAndSend(eq("/topic/project/10"), any(Object.class));
    }

    @Test
    void createTask_withAssignee_publishesAssignmentEvent() {
        User assignee = User.builder().id(2L).email("assignee@example.com").fullName("Assignee").build();

        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Review PR");
        request.setProjectId(10L);
        request.setAssigneeId(2L);

        when(projectService.getProjectOrThrow(10L)).thenReturn(project);
        when(userRepository.findByEmail("creator@example.com")).thenReturn(Optional.of(creator));
        when(userRepository.findById(2L)).thenReturn(Optional.of(assignee));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
            Task t = inv.getArgument(0);
            t.setId(101L);
            return t;
        });

        taskService.createTask(request, "creator@example.com");

        ArgumentCaptor<TaskAssignedEvent> captor = ArgumentCaptor.forClass(TaskAssignedEvent.class);
        verify(taskEventPublisher).publishTaskAssigned(captor.capture());

        assertThat(captor.getValue().getAssigneeEmail()).isEqualTo("assignee@example.com");
        assertThat(captor.getValue().getTaskTitle()).isEqualTo("Review PR");
    }

    @Test
    void updateStatus_savesNewStatus_andBroadcastsEvent() {
        Task task = Task.builder().id(200L).title("Old task").project(project).status(TaskStatus.TODO).build();

        when(taskRepository.findById(200L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.updateStatus(200L, TaskStatus.DONE);

        assertThat(result.getStatus()).isEqualTo(TaskStatus.DONE);
        verify(messagingTemplate).convertAndSend(eq("/topic/project/10"), any(Object.class));
    }
}
