package ec.udla.iswz2202.bikestoreasync.consumer;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderConsumer {

    @RabbitListener(queues = "orders.queue")
    public void receiveMessage(String message) {
        // Regla simple: si el mensaje contiene "dlq" o "fail", lo mandamos a la DLQ.
        String lower = message.toLowerCase();
        if (lower.contains("dlq") || lower.contains("fail")) {
            System.out.println("⚠️ Rechazando mensaje y enviando a DLQ: " + message);
            throw new AmqpRejectAndDontRequeueException("Poison message -> DLQ");
        }

        System.out.println("✅ Mensaje recibido desde RabbitMQ: " + message);
    }
}
