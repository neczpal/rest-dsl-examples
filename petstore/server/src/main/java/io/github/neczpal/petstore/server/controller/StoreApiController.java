package io.github.neczpal.petstore.server.controller;

import io.github.neczpal.petstore.server.Order;
import io.github.neczpal.petstore.server.StoreApi;
import io.github.neczpal.petstore.server.data.OrderEntity;
import io.github.neczpal.petstore.server.data.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class StoreApiController implements StoreApi {

    private final OrderRepository orderRepository;

    public StoreApiController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    private OrderEntity toEntity(Order order) {
        if (order == null) return null;
        OrderEntity entity = new OrderEntity();
        entity.setId(order.id());
        entity.setPetId(order.petId());
        entity.setQuantity(order.quantity());
        entity.setShipDate(order.shipDate());
        entity.setStatus(order.status());
        entity.setComplete(order.complete());
        return entity;
    }

    private Order toDto(OrderEntity entity) {
        if (entity == null) return null;
        return new Order(
                entity.getId(),
                entity.getPetId(),
                entity.getQuantity(),
                entity.getShipDate(),
                entity.getStatus(),
                entity.getComplete()
        );
    }

    @Override
    public ResponseEntity<String> getInventory() {
        log.info("Fetching store inventory");
        return ResponseEntity.ok("{\"available\": 10, \"pending\": 2}");
    }

    @Override
    public ResponseEntity<Order> placeOrder(Order body) {
        log.info("Placing a new order for pet ID: {}", body.petId());
        OrderEntity saved = orderRepository.save(toEntity(body));
        log.info("Successfully placed order with ID: {}", saved.getId());
        return ResponseEntity.ok(toDto(saved));
    }

    @Override
    public ResponseEntity<Order> getOrderById(Integer orderId) {
        log.info("Searching for order with ID: {}", orderId);
        return orderRepository.findById(orderId)
                .map(entity -> {
                    log.info("Found order with ID: {}", orderId);
                    return ResponseEntity.ok(toDto(entity));
                })
                .orElseGet(() -> {
                    log.warn("Order with ID {} not found.", orderId);
                    return ResponseEntity.notFound().build();
                });
    }

    @Override
    public ResponseEntity<Void> deleteOrder(Integer orderId) {
        log.info("Attempting to delete order with ID: {}", orderId);
        if (orderRepository.existsById(orderId)) {
            orderRepository.deleteById(orderId);
            log.info("Successfully deleted order with ID: {}", orderId);
            return ResponseEntity.ok().build();
        }
        log.warn("Order with ID {} not found for deletion.", orderId);
        return ResponseEntity.notFound().build();
    }
}
