package org.example.user.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserEventConsumer {

    @KafkaListener(topics = "order-events", groupId = "user-service-group")
    public void handleOrderEvent(@Payload Map<String, Object> orderEvent, 
                                @Header(KafkaHeaders.KEY) String eventType) {
        
        try {
            String eventTypeValue = orderEvent.get("eventType").toString();
            Long userId = Long.valueOf(orderEvent.get("userId").toString());
            
            switch (eventTypeValue) {
                case "ORDER_CREATED":
                    handleOrderCreated(userId, orderEvent);
                    break;
                case "ORDER_STATUS_UPDATED":
                    handleOrderStatusUpdated(userId, orderEvent);
                    break;
                case "PAYMENT_STATUS_UPDATED":
                    handlePaymentStatusUpdated(userId, orderEvent);
                    break;
                default:
                    System.out.println("Unknown order event type: " + eventTypeValue);
            }
        } catch (Exception e) {
            System.err.println("Error processing order event: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "cart-events", groupId = "user-service-group")
    public void handleCartEvent(@Payload Map<String, Object> cartEvent, 
                               @Header(KafkaHeaders.KEY) String eventType) {
        
        try {
            String eventTypeValue = cartEvent.get("eventType").toString();
            Long userId = Long.valueOf(cartEvent.get("userId").toString());
            
            switch (eventTypeValue) {
                case "ITEM_ADDED":
                    handleCartItemAdded(userId, cartEvent);
                    break;
                case "CHECKOUT_INITIATED":
                    handleCheckoutInitiated(userId, cartEvent);
                    break;
                default:
                    System.out.println("Cart event received for user: " + userId + ", type: " + eventTypeValue);
            }
        } catch (Exception e) {
            System.err.println("Error processing cart event: " + e.getMessage());
        }
    }

    private void handleOrderCreated(Long userId, Map<String, Object> event) {
        Long orderId = Long.valueOf(event.get("orderId").toString());
        System.out.println("Order created for user: " + userId + ", order ID: " + orderId);
        
        // Here you could update user statistics, loyalty points, etc.
        // For example: update user's order count, total spent, etc.
    }

    private void handleOrderStatusUpdated(Long userId, Map<String, Object> event) {
        Long orderId = Long.valueOf(event.get("orderId").toString());
        System.out.println("Order status updated for user: " + userId + ", order ID: " + orderId);
        
        // Here you could trigger notifications, update user activity, etc.
    }

    private void handlePaymentStatusUpdated(Long userId, Map<String, Object> event) {
        Long orderId = Long.valueOf(event.get("orderId").toString());
        System.out.println("Payment status updated for user: " + userId + ", order ID: " + orderId);
        
        // Here you could update user payment history, handle failed payments, etc.
    }

    private void handleCartItemAdded(Long userId, Map<String, Object> event) {
        Long productId = Long.valueOf(event.get("productId").toString());
        Integer quantity = Integer.valueOf(event.get("quantity").toString());
        
        System.out.println("User " + userId + " added product " + productId + " (qty: " + quantity + ") to cart");
        
        // Here you could track user behavior, update recommendations, etc.
    }

    private void handleCheckoutInitiated(Long userId, Map<String, Object> event) {
        Integer totalItems = Integer.valueOf(event.get("quantity").toString());
        
        System.out.println("User " + userId + " initiated checkout with " + totalItems + " items");
        
        // Here you could track conversion funnel, send abandoned cart emails, etc.
    }
}
