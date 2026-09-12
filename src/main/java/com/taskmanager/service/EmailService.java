package com.taskmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendTaskAssignedEmail(String toEmail, String taskTitle, String projectName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("You've been assigned a task: " + taskTitle);
        message.setText(
                "Hi,\n\n" +
                "You've been assigned a new task in project \"" + projectName + "\":\n\n" +
                "  " + taskTitle + "\n\n" +
                "Log in to view details.\n\n" +
                "- TaskManager"
        );
        mailSender.send(message);
    }
}
