package com.solidarize.config;

import com.rabbitmq.client.ConnectionFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

@Configuration
public class RabbitMqConfiguration implements RabbitListenerConfigurer {

    private static final int SHUTDOWN_TIMEOUT = 60000;
    private static final String TLSV1_2 = "TLSv1.2";

    @Value("${rabbitmq.uri}")
    String uri;

    @Bean
    @Primary
    public CachingConnectionFactory connectionFactory() {
        ConnectionFactory factory = new ConnectionFactory();

        try {
            factory.setUri(uri);
            if (factory.isSSL()) {
                factory.useSslProtocol(TLSV1_2);
            }
        } catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException e) {
            throw new BeanCreationException("Failed to create Rabbit MQ connection factory", e);
        }

        return new CachingConnectionFactory(factory);
    }

    @Bean
    public RabbitAdmin rabbitAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory());
        template.setChannelTransacted(true);
        template.setMandatory(true);
        template.setMessageConverter(converter());

        return template;
    }

    @Bean
    public Jackson2JsonMessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public DefaultMessageHandlerMethodFactory defaultHandlerMethodFactory() {
        DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        factory.setMessageConverter(converter);
        return factory;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory() {
            @Override
            protected void initializeContainer(SimpleMessageListenerContainer instance) {
                super.initializeContainer(instance);
                instance.setShutdownTimeout(SHUTDOWN_TIMEOUT);
            }
        };
        factory.setConnectionFactory(connectionFactory());
        return factory;
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        registrar.setMessageHandlerMethodFactory(defaultHandlerMethodFactory());
    }
}
