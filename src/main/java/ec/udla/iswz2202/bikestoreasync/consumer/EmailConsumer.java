package ec.udla.iswz2202.bikestoreasync.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {

    // Escucha la cola "emails.queue"
    @RabbitListener(queues = "emails.queue")
    public void receiveEmail(String message) {
        System.out.println("📧 [EmailConsumer] Email recibido -> " + message);
        // Aquí podrías “simular” envío de email, validar, etc.
    }
}
