package com.taskmanager.service;

import com.taskmanager.entity.Project;
import com.taskmanager.entity.User;
import com.taskmanager.exception.ApiException;
import com.taskmanager.repository.ProjectRepository;
import com.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @CacheEvict(value = "projects", allEntries = true) // a new project could show up in anyone's list
    public Project createProject(String name, String description, String ownerEmail) {
        User owner = getUserByEmail(ownerEmail);

        Project project = Project.builder()
                .name(name)
                .description(description)
                .owner(owner)
                .build();

        return projectRepository.save(project);
    }

    public List<Project> getMyProjects(String userEmail) {
        User user = getUserByEmail(userEmail);

        return Stream.concat(
                projectRepository.findByOwnerId(user.getId()).stream(),
                projectRepository.findByMembers_Id(user.getId()).stream()
        ).distinct().collect(Collectors.toList());
    }

    @CacheEvict(value = "project", key = "#projectId")
    public Project addMember(Long projectId, Long userId, String requesterEmail) {
        Project project = getProjectOrThrow(projectId);
        assertIsOwner(project, requesterEmail);

        User newMember = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        project.getMembers().add(newMember);
        return projectRepository.save(project);
    }

    @Cacheable(value = "project", key = "#id")
    public Project getProjectOrThrow(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    private void assertIsOwner(Project project, String requesterEmail) {
        if (!project.getOwner().getEmail().equals(requesterEmail)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the project owner can perform this action");
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
