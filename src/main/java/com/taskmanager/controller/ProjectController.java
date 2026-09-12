package com.taskmanager.controller;

import com.taskmanager.entity.Project;
import com.taskmanager.service.ProjectService;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<Project> create(@RequestBody CreateProjectRequest request, Authentication auth) {
        Project project = projectService.createProject(request.getName(), request.getDescription(), auth.getName());
        return ResponseEntity.ok(project);
    }

    @GetMapping
    public ResponseEntity<List<Project>> myProjects(Authentication auth) {
        return ResponseEntity.ok(projectService.getMyProjects(auth.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectOrThrow(id));
    }

    @PostMapping("/{id}/members/{userId}")
    public ResponseEntity<Project> addMember(@PathVariable Long id, @PathVariable Long userId, Authentication auth) {
        return ResponseEntity.ok(projectService.addMember(id, userId, auth.getName()));
    }

    @Data
    public static class CreateProjectRequest {
        @NotBlank
        private String name;
        private String description;
    }
}
