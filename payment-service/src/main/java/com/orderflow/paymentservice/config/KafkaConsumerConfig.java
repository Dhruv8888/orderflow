package com.orderflow.paymentservice.config;

import com.orderflow.paymentservice.event.PaymentRequestedEvent;
import com.orderflow.paymentservice.event.RefundPaymentEvent;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private Map<String, Object> baseConsumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("group.id", "payment-service");
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
    public ConcurrentKafkaListenerContainerFactory<String, PaymentRequestedEvent> paymentRequestedFactory() {
        return factoryFor(PaymentRequestedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RefundPaymentEvent> refundPaymentFactory() {
        return factoryFor(RefundPaymentEvent.class);
    }
}