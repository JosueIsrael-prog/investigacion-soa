package ec.udla.iswz2202.bikestoreasync.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderProducer {

    private final RabbitTemplate rabbitTemplate;

    // Se inyectan desde application.yml
    @Value("${app.exchange}")
    private String exchangeName;

    @Value("${app.rk.orderCreated}")
    private String rkOrderCreated;

    public OrderProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // Endpoint GET para probar fácilmente desde el navegador
    // Ejemplo: http://localhost:8080/api/orders/send?msg=hola
    @GetMapping("/send")
    public String sendOrder(@RequestParam String msg) {
        String payload = "[BIKESTORE] Mensaje: " + msg;
        rabbitTemplate.convertAndSend(exchangeName, rkOrderCreated, payload);
        System.out.println("➡️ Enviado a Exchange=" + exchangeName + ", RoutingKey=" + rkOrderCreated + ", Payload=" + payload);
        return "📦 Mensaje enviado correctamente: " + payload;
    }

    // Endpoint POST para enviar JSON desde Postman
    @PostMapping
    public String sendOrderJson(@RequestBody String orderJson) {
        rabbitTemplate.convertAndSend(exchangeName, rkOrderCreated, orderJson);
        System.out.println("➡️ JSON enviado a Exchange=" + exchangeName + ", RoutingKey=" + rkOrderCreated + ", Body=" + orderJson);
        return "📦 Pedido publicado correctamente";
    }
}
