package com.amani.energytrading.position;

import com.amani.energytrading.event.TradeBookedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class PositionService {

    private static final Logger log = LoggerFactory.getLogger(PositionService.class);

    private final PositionRepository positionRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final Clock clock;

    public PositionService(PositionRepository positionRepository,
                           ProcessedEventRepository processedEventRepository,
                           Clock clock) {
        this.positionRepository = positionRepository;
        this.processedEventRepository = processedEventRepository;
        this.clock = clock;
    }

    /**
     * Applies a booked trade to the matching position. Kafka delivers at least once, so the event id
     * is recorded in the same transaction as the position update: a redelivered event is ignored.
     *
     * @return {@code true} if the event changed the position, {@code false} if it was a duplicate
     */
    @Transactional
    public boolean apply(TradeBookedEvent event) {
        if (processedEventRepository.existsById(event.eventId())) {
            log.info("Event {} already processed, skipping", event.eventId());
            return false;
        }

        Instant now = Instant.now(clock);
        Position position = positionRepository
                .findByPortfolioAndCommodityAndDeliveryDate(event.portfolio(), event.commodity(), event.deliveryDate())
                .orElseGet(() -> Position.open(event.portfolio(), event.commodity(), event.deliveryDate(), now));

        position.apply(event.direction(), event.quantityMwh(), event.priceEurPerMwh(), now);
        positionRepository.save(position);
        processedEventRepository.save(new ProcessedEvent(event.eventId(), now));
        return true;
    }

    @Transactional(readOnly = true)
    public List<Position> findByPortfolio(String portfolio) {
        return positionRepository.findByPortfolioOrderByDeliveryDateAscCommodityAsc(portfolio);
    }
}
