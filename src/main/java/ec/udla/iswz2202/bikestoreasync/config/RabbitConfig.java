package ec.udla.iswz2202.bikestoreasync.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RabbitConfig {

    @Value("${app.exchange}")
    private String exchangeName;

    @Value("${app.queues.orders}")
    private String ordersQueueName;

    @Value("${app.queues.email}")
    private String emailQueueName;

    @Value("${app.queues.dlq}")
    private String dlqName;

    @Value("${app.rk.orderCreated}")
    private String rkOrderCreated;

    @Value("${app.rk.emailSend}")
    private String rkEmailSend;

    // Exchange principal
    @Bean
    public TopicExchange ordersExchange() {
        return ExchangeBuilder.topicExchange(exchangeName).durable(true).build();
    }

    // Cola principal con DLQ configurada
    @Bean
    public Queue ordersQueue() {
        return QueueBuilder.durable(ordersQueueName)
                .withArguments(Map.of(
                        "x-dead-letter-exchange", exchangeName,
                        "x-dead-letter-routing-key", dlqName
                ))
                .build();
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(emailQueueName).build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(dlqName).build();
    }

    @Bean
    public Binding bindOrders() {
        return BindingBuilder.bind(ordersQueue()).to(ordersExchange()).with(rkOrderCreated);
    }

    @Bean
    public Binding bindEmail() {
        return BindingBuilder.bind(emailQueue()).to(ordersExchange()).with(rkEmailSend);
    }

    @Bean
    public Binding bindDlq() {
        return BindingBuilder.bind(deadLetterQueue()).to(ordersExchange()).with(dlqName);
    }

    // Conversor JSON
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate con soporte JSON
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf, Jackson2JsonMessageConverter conv) {
        RabbitTemplate tpl = new RabbitTemplate(cf);
        tpl.setMessageConverter(conv);
        return tpl;
    }

    // Listener con reintentos y DLQ automático
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory cf, Jackson2JsonMessageConverter conv) {

        SimpleRabbitListenerContainerFactory f = new SimpleRabbitListenerContainerFactory();
        f.setConnectionFactory(cf);
        f.setMessageConverter(conv);
        f.setDefaultRequeueRejected(false); // No reencolar → DLQ activa

        // Reintentos automáticos: 3 intentos y luego DLQ
        f.setAdviceChain(
                RetryInterceptorBuilder.stateless()
                        .maxAttempts(3)
                        .recoverer(new RejectAndDontRequeueRecoverer())
                        .build()
        );

        return f;
    }
}
