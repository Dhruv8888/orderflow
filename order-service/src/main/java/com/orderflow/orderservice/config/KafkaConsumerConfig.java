package com.orderflow.orderservice.config;

import com.orderflow.orderservice.event.OrderShippedEvent;
import com.orderflow.orderservice.event.PaymentCompletedEvent;
import com.orderflow.orderservice.event.PaymentFailedEvent;
import com.orderflow.orderservice.event.ShipmentRequestedEvent;
import com.orderflow.orderservice.event.StockReservationFailedEvent;
import com.orderflow.orderservice.event.StockReservedEvent;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import com.orderflow.orderservice.event.ShipmentFailedEvent;
import com.orderflow.orderservice.event.StockReleasedEvent;
import com.orderflow.orderservice.event.PaymentRefundedEvent;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private Map<String, Object> baseConsumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("group.id", "order-service");
        props.put("auto.offset.reset", "earliest");
        props.put("key.deserializer", StringDeserializer.class);
        return props;
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> factoryFor(Class<T> targetType) {
        JsonDeserializer<T> deserializer = new JsonDeserializer<>(targetType);
        deserializer.setUseTypeHeaders(false);
        deserializer.addTrustedPackages("*");

        ConsumerFactory<String, T> cf =
                new DefaultKafkaConsumerFactory<>(baseConsumerProps(), new StringDeserializer(), deserializer);

        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, StockReservedEvent> stockReservedFactory() {
        return factoryFor(StockReservedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, StockReservationFailedEvent> stockReservationFailedFactory() {
        return factoryFor(StockReservationFailedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentCompletedEvent> paymentCompletedFactory() {
        return factoryFor(PaymentCompletedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentFailedEvent> paymentFailedFactory() {
        return factoryFor(PaymentFailedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ShipmentRequestedEvent> shipmentRequestedFactory() {
        return factoryFor(ShipmentRequestedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderShippedEvent> orderShippedFactory() {
        return factoryFor(OrderShippedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ShipmentFailedEvent> shipmentFailedFactory() {
        return factoryFor(ShipmentFailedEvent.class);
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, StockReleasedEvent> stockReleasedFactory() {
        return factoryFor(StockReleasedEvent.class);
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentRefundedEvent> paymentRefundedFactory() {
        return factoryFor(PaymentRefundedEvent.class);
    }
}