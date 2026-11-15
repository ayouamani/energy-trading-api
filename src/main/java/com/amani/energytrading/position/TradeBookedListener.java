package com.amani.energytrading.position;

import com.amani.energytrading.event.TradeBookedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TradeBookedListener {

    private final PositionService positionService;

    public TradeBookedListener(PositionService positionService) {
        this.positionService = positionService;
    }

    @KafkaListener(topics = "${app.kafka.topics.trades-booked}")
    public void on(TradeBookedEvent event) {
        positionService.apply(event);
    }
}
