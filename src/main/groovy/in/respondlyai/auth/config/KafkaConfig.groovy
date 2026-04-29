package in.respondlyai.auth.config

import in.respondlyai.auth.dto.OrganizationCreatedEvent
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer

@Configuration
@EnableKafka
class KafkaConfig {

    @Value('${spring.kafka.bootstrap-servers}')
    private String bootstrapServers

    @Bean
    ConsumerFactory<String, OrganizationCreatedEvent> consumerFactory() {
        JsonDeserializer<OrganizationCreatedEvent> deserializer = new JsonDeserializer<>(OrganizationCreatedEvent.class)
        deserializer.addTrustedPackages("*")
        deserializer.setUseTypeHeaders(false)

        Map<String, Object> props = [:]
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "auth-group")
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class)
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class)
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer)
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, OrganizationCreatedEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrganizationCreatedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>()
        factory.setConsumerFactory(consumerFactory())
        return factory
    }
}
