package com.taskmanager.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String TASK_EXCHANGE = "task.exchange";
    public static final String TASK_ASSIGNED_QUEUE = "task.assigned.queue";
    public static final String TASK_ASSIGNED_ROUTING_KEY = "task.assigned";

    @Bean
    public TopicExchange taskExchange() {
        return new TopicExchange(TASK_EXCHANGE);
    }

    @Bean
    public Queue taskAssignedQueue() {
        // durable=true: survives a broker restart, so no assignment notification gets lost
        return new Queue(TASK_ASSIGNED_QUEUE, true);
    }

    @Bean
    public Binding taskAssignedBinding(Queue taskAssignedQueue, TopicExchange taskExchange) {
        return BindingBuilder.bind(taskAssignedQueue).to(taskExchange).with(TASK_ASSIGNED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        // Publishes/consumes messages as JSON instead of raw Java serialization
        return new Jackson2JsonMessageConverter();
    }
}
