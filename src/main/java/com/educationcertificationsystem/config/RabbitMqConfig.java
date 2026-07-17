package com.educationcertificationsystem.config;

import com.educationcertificationsystem.constant.NoticeMqConstants;
import com.educationcertificationsystem.constant.SurveyMqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMqConfig {

    @Bean
    public TopicExchange noticeExchange() {
        return new TopicExchange(NoticeMqConstants.NOTICE_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange surveyExchange() {
        return new TopicExchange(SurveyMqConstants.SURVEY_EXCHANGE, true, false);
    }

    @Bean
    public Queue noticeQueue() {
        return new Queue(NoticeMqConstants.NOTICE_QUEUE, true);
    }

    @Bean
    public Queue surveyQueue() {
        return new Queue(SurveyMqConstants.SURVEY_QUEUE, true);
    }

    @Bean
    public Binding noticeBinding(Queue noticeQueue, TopicExchange noticeExchange) {
        return BindingBuilder.bind(noticeQueue)
                .to(noticeExchange)
                .with(NoticeMqConstants.NOTICE_ROUTING_KEY);
    }

    @Bean
    public Binding surveyBinding(Queue surveyQueue, TopicExchange surveyExchange) {
        return BindingBuilder.bind(surveyQueue)
                .to(surveyExchange)
                .with(SurveyMqConstants.SURVEY_ROUTING_KEY);
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         JacksonJsonMessageConverter jacksonJsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jacksonJsonMessageConverter);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            JacksonJsonMessageConverter jacksonJsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jacksonJsonMessageConverter);
        return factory;
    }
}
