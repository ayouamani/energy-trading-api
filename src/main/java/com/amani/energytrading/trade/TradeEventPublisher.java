package com.amani.energytrading.trade;

import com.amani.energytrading.event.TradeBookedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Bridges the domain event to Kafka once the database transaction is committed,
 * so consumers never see a trade that was rolled back.
 * Messages are keyed by portfolio to keep the ordering of trades per portfolio.
 */
@Component
public class TradeEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TradeEventPublisher.class);

    private final KafkaTemplate<String, TradeBookedEvent> kafkaTemplate;
    private final String topic;

    public TradeEventPublisher(KafkaTemplate<String, TradeBookedEvent> kafkaTemplate,
                               @Value("${app.kafka.topics.trades-booked}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(TradeBookedEvent event) {
        kafkaTemplate.send(topic, event.portfolio(), event)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        log.error("Failed to publish event {} for trade {}", event.eventId(), event.tradeId(), error);
                    } else {
                        log.debug("Published event {} to {}", event.eventId(), result.getRecordMetadata());
                    }
                });
    }
}
